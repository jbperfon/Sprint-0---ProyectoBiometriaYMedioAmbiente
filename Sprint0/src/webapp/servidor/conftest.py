# servidor/conftest.py
from pathlib import Path
import sys

ROOT = Path(__file__).parent.resolve()
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))