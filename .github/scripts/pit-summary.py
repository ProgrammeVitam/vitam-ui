#!/usr/bin/env python3
"""Build a Markdown summary (mutation score per module) from PIT XML reports (target/pit-reports/mutations.xml)."""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

KILLED_STATUSES = {"KILLED", "TIMED_OUT", "MEMORY_ERROR"}


def main(root: Path) -> None:
    rows = []
    for report in sorted(root.glob("**/target/pit-reports/mutations.xml")):
        module = report.parent.parent.parent.relative_to(root).as_posix()
        mutations = ET.parse(report).getroot().findall("mutation")
        total = len(mutations)
        killed = sum(1 for m in mutations if m.get("status") in KILLED_STATUSES)
        no_coverage = sum(1 for m in mutations if m.get("status") == "NO_COVERAGE")
        covered = total - no_coverage
        rows.append((module, total, killed, no_coverage, covered))

    print("## Mutation testing (PIT)\n")
    if not rows:
        print("No PIT report found.")
        return

    print("| Module | Mutations | Killed | No coverage | Mutation score | Test strength |")
    print("|---|---:|---:|---:|---:|---:|")
    for module, total, killed, no_coverage, covered in rows:
        print(f"| `{module}` | {total} | {killed} | {no_coverage} | {percent(killed, total)} | {percent(killed, covered)} |")
    total = sum(r[1] for r in rows)
    killed = sum(r[2] for r in rows)
    no_coverage = sum(r[3] for r in rows)
    covered = sum(r[4] for r in rows)
    print(
        f"| **Total** | **{total}** | **{killed}** | **{no_coverage}** "
        f"| **{percent(killed, total)}** | **{percent(killed, covered)}** |"
    )
    print("\n_Mutation score = killed / mutations. Test strength = killed / covered mutations._")


def percent(value: int, total: int) -> str:
    return f"{100 * value / total:.0f}%" if total else "-"


if __name__ == "__main__":
    main(Path(sys.argv[1] if len(sys.argv) > 1 else "."))
