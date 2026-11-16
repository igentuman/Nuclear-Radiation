#!/usr/bin/env python3
"""Seed two old NTM SavedData row in an OFFLINE disposable smoke world.

Run a disposable server once and stop it normally, then invoke:
  python3 src/test/resources/ntm-seed-legacy-world.py /tmp/nr-ntm-migration-core
Install ntm-world-migration-smoke.js and launch that directory twice more.
This fixture uses only the Python standard library and is never packaged.
"""

import fcntl
import gzip
import hashlib
import json
from pathlib import Path
import shutil
import struct
import sys


def named(tag_type, name, payload):
    encoded = name.encode("utf-8")
    return bytes([tag_type]) + struct.pack(">H", len(encoded)) + encoded + payload


def seed(run_directory):
    world = run_directory.resolve() / "world"
    marker = world / "nr_ntm_migration_smoke_seeded"
    if marker.exists():
        raise RuntimeError("This fixture was already seeded; use another disposable directory")
    with (world / "session.lock").open("r+b") as lock:
        # Refuse to edit an active server. This fixture targets the Linux development environment.
        fcntl.lockf(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        target = world / "dimensions/minecraft/overworld/data/hbm/hbm_radiation.dat"
        if target.exists():
            original = target.read_bytes()
            raw = gzip.decompress(original)
            expected_prefix = named(10, "", named(10, "data", named(9, "chunks", bytes(5)) + b"\0"))
            if not raw.startswith(expected_prefix):
                raise RuntimeError("Refusing to replace nonempty or unfamiliar NTM SavedData")
            shutil.copy2(target, target.with_suffix(".dat.fixture-original"))
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
        # The pinned 26.1.2 save wrapper, and RadiationSavedData's c/s/s/d codec fields.
        section = named(3, "s", struct.pack(">i", 22))
        section += named(9, "d", b"\x06" + struct.pack(">id", 1, 42.0)) + b"\0"
        chunk = named(4, "c", struct.pack(">q", 48 | (49 << 32)))
        chunk += named(9, "s", b"\x0a" + struct.pack(">i", 1) + section) + b"\0"
        later = named(4, "c", struct.pack(">q", 50 | (51 << 32)))
        later += named(9, "s", b"\x0a" + struct.pack(">i", 1) + section) + b"\0"
        data = named(9, "chunks", b"\x0a" + struct.pack(">i", 2) + chunk + later) + b"\0"
        root = named(10, "", named(10, "data", data)
                     + named(3, "DataVersion", struct.pack(">i", 4790)) + b"\0")
        compressed = gzip.compress(root, mtime=0)
        target.write_bytes(compressed)
        marker.write_text(json.dumps({"validated": False, "source_sha256": hashlib.sha256(compressed).hexdigest()}))
        print(f"Seeded old NTM radiation at chunk (48, 49), section Y=18: {target}")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Usage: ntm-seed-legacy-world.py DISPOSABLE_RUN_DIRECTORY (server must be stopped)")
    seed(Path(sys.argv[1]))
