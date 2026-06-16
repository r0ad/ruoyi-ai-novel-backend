# -*- coding: utf-8 -*-
"""Generate novel module Java/XML sources as UTF-8 (LF, no BOM)."""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
FILES = {}


def add(rel: str, content: str) -> None:
    FILES[rel] = content.rstrip("
") + "
"


def write_all() -> list[Path]:
    written = []
    for rel, content in FILES.items():
        path = ROOT / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="
")
        written.append(path)
        print("wrote", rel)
    return written


def validate_utf8(paths: list[Path]) -> None:
    errors = []
    for path in paths:
        data = path.read_bytes()
        if data.startswith(b"ï»¿"):
            errors.append(f"{path}: UTF-8 BOM detected")
            continue
        try:
            text = data.decode("utf-8")
        except UnicodeDecodeError as e:
            errors.append(f"{path}: {e}")
            continue
        if "�" in text:
            errors.append(f"{path}: replacement character (mojibake)")
    if errors:
        for err in errors:
            print("VALIDATION FAILED:", err, file=sys.stderr)
        sys.exit(1)
    print("UTF-8 validation passed for", len(paths), "file(s)")


