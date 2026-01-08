# GitHub Actions CLI (ghacli)

A command-line tool for analyzing GitHub Actions workflow runs, calculating duration statistics, and exporting metrics to CSV files.

## Features

- 📊 Calculate mean, median, and P90 statistics for workflow, job, and step durations
- 📅 Filter workflow runs by date range
- 🔄 Handle reruns (include, exclude, or only reruns)
- 📁 Export comprehensive metrics to CSV files:
  - Overall metrics summary
  - Per-job metrics (with all steps)
  - Jobs summary
  - Steps summary

## Installation

```
curl -L https://github.com/cdsap/GHAWorkflowsCli/releases/download/v0.0.2/ghacli  --output ghacli
chmod 0757 ghacli
```

## Usage

### Basic Usage

After installing the CLI, run:

```bash
./ghacli --token YOUR_TOKEN --owner OWNER --repo REPO --workflow-id WORKFLOW_ID
```

### Required Parameters

- `--token`: GitHub personal access token (or set `GITHUB_TOKEN` environment variable)
- `--owner`: Repository owner (e.g., `gradle`, `octocat`)
- `--repo`: Repository name (e.g., `Hello-World`)
- `--workflow-id`: Workflow ID or filename (e.g., `ci.yml`, `build-verification.yml`)

### Optional Parameters

- `--branch`: Filter by branch name (e.g., `main`, `develop`)
- `--max-builds`: Maximum number of workflow runs to analyze (default: 10)
- `--from-date`: Filter workflow runs from this date (format: `YYYY-MM-DD`, e.g., `2024-01-01`)
- `--to-date`: Filter workflow runs to this date (format: `YYYY-MM-DD`, e.g., `2024-12-31`)
- `--only-success`: Only process successful workflow runs
- `--exclude-reruns`: Exclude reruns, only process original workflow runs
- `--only-reruns`: Only process reruns (exclude original runs)
- `--concurrent-calls`: Number of concurrent API calls (default: 8)

### Examples

#### Basic Analysis

```bash
ghacli \
  --token $GITHUB_TOKEN \
  --owner gradle \
  --repo android-cache-fix-gradle-plugin \
  --branch main \
  --workflow-id build-verification.yml \
  --max-builds 100
```

#### Filter by Date Range and Success Status

```bash
ghacli \
  --token $GITHUB_TOKEN \
  --owner gradle \
  --repo android-cache-fix-gradle-plugin \
  --branch main \
  --workflow-id build-verification.yml \
  --max-builds 100 \
  --to-date 2026-01-06 \
  --only-success
```

#### Exclude Reruns

```bash
ghacli \
  --token $GITHUB_TOKEN \
  --owner gradle \
  --repo android-cache-fix-gradle-plugin \
  --branch main \
  --workflow-id build-verification.yml \
  --max-builds 100 \
  --exclude-reruns \
  --only-success
```

#### Date Range Analysis

```bash
ghacli \
  --token $GITHUB_TOKEN \
  --owner gradle \
  --repo android-cache-fix-gradle-plugin \
  --workflow-id build-verification.yml \
  --from-date 2025-12-01 \
  --to-date 2026-01-06 \
  --max-builds 200 \
  --only-success
```

## Examples Output

### Console Output

```
Filters applied:
  - Excluding reruns (only original runs)
  - Only successful workflows
  - Date range: 2025-12-01 to 2026-01-06
Total workflow runs found: 150
Workflow runs after filters: 85
  - Original runs: 70
  - Reruns: 15
Analyzing 85 workflow run(s)...

============================================================
WORKFLOW DURATION STATISTICS
============================================================
Mean:   5m 30s (330s)
Median: 5m 15s (315s)
P90:    7m 45s (465s)
Count:  85

============================================================
JOB DURATION STATISTICS
============================================================
Mean:   2m 15s (135s)
Median: 2m 10s (130s)
P90:    3m 20s (200s)
Count:  255

============================================================
STEP DURATION STATISTICS
============================================================
Mean:   15s (15s)
Median: 14s (14s)
P90:    25s (25s)
Count:  850

============================================================
All metrics exported to: output/owner-repo-workflowId-timestamp/

Files created:
  - workflow_summary.csv
Per-job metrics (including all steps):
  - gha_metrics_job_build.csv
  - gha_metrics_job_test.csv
  - gha_metrics_job_deploy.csv

Summary files:
  - gha_jobs_summary.csv (jobs summary)
  - gha_steps_summary.csv (steps summary)
============================================================
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

