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

### Build the CLI

1. Clone the repository or navigate to the project directory
2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Install the distribution:
   ```bash
   ./gradlew installDist
   ```

4. The CLI executable will be available at:
   ```
   build/install/ghacli/bin/ghacli
   ```

5. Run the CLI:
   ```bash
   build/install/ghacli/bin/ghacli [options]
   ```

6. (Optional) Add to your PATH:
   ```bash
   # Add to PATH (add to ~/.bashrc or ~/.zshrc)
   export PATH=$PATH:$(pwd)/build/install/ghacli/bin
   ```

   Then you can use `ghacli` from anywhere.

## Usage

### Basic Usage

After installing the CLI, run:

```bash
build/install/ghacli/bin/ghacli --token YOUR_TOKEN --owner OWNER --repo REPO
```

Or if you've added it to your PATH:

```bash
ghacli --token YOUR_TOKEN --owner OWNER --repo REPO
```

### Required Parameters

- `--token`: GitHub personal access token (or set `GITHUB_TOKEN` environment variable)
- `--owner`: Repository owner (e.g., `gradle`, `octocat`)
- `--repo`: Repository name (e.g., `Hello-World`)

### Optional Parameters

- `--branch`: Filter by branch name (e.g., `main`, `develop`)
- `--workflow-id`: Workflow ID or filename (e.g., `ci.yml`, `build-verification.yml`)
- `--max-builds`: Maximum number of workflow runs to analyze (default: 10)
- `--csv-output`: Output CSV file path (default: `gha_metrics.csv`)
- `--from-date`: Filter workflow runs from this date (format: `YYYY-MM-DD`, e.g., `2024-01-01`)
- `--to-date`: Filter workflow runs to this date (format: `YYYY-MM-DD`, e.g., `2024-12-31`)
- `--only-success`: Only process successful workflow runs
- `--exclude-reruns`: Exclude reruns, only process original workflow runs
- `--only-reruns`: Only process reruns (exclude original runs)
- `--concurrent-calls`: Number of concurrent API calls (default: 4)

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
  --from-date 2025-12-01 \
  --to-date 2026-01-06 \
  --max-builds 200 \
  --only-success
```

#### Using Environment Variable for Token

```bash
export GITHUB_TOKEN=your_token_here

ghacli \
  --owner gradle \
  --repo android-cache-fix-gradle-plugin \
  --branch main \
  --workflow-id build-verification.yml \
  --max-builds 100
```

**Note:** If you haven't added it to your PATH, use the full path: `build/install/ghacli/bin/ghacli`

## Output Files

The tool creates an `output` folder in the project root. Each invocation creates a subfolder with the format:
```
output/owner-repo-workflowId-timestamp/
```

For example:
```
output/gradle-android-cache-fix-gradle-plugin-build-verification.yml-20260107-143022/
```

The timestamp format is `yyyyMMdd-HHmmss` (e.g., `20260107-143022` for January 7, 2026 at 14:30:22).

All CSV files for that run are saved in this folder:

### 1. Overall Metrics (`gha_metrics.csv`)
Summary statistics for all workflows, jobs, and steps.

### 2. Per-Job Metrics (`gha_metrics_job_<job_name>.csv`)
Detailed metrics for each job, including:
- Job duration statistics
- Aggregated step statistics
- Individual step statistics with success rates

### 3. Master Metrics (`gha_master_metrics.csv`)
Comprehensive file with all metrics organized by category:
- Workflow Level Metrics
- Job Level Metrics (overall and individual)
- Step Level Metrics (overall, by job, and individual)

### 4. Jobs Summary (`gha_jobs_summary.csv`)
One row per job with duration statistics and overall step success rate.

### 5. Steps Summary (`gha_steps_summary.csv`)
One row per step (job + step name) with duration statistics and success rate.

### 6. Success Rates Summary (`gha_success_rates_summary.csv`)
Success/failure breakdown by:
- Overall (all steps)
- By job
- By individual step

## CSV Format

All CSV files include:
- **Mean**: Average duration
- **Median**: Middle value duration
- **P90**: 90th percentile duration
- **Count**: Number of samples used for statistics
- **Success Count**: Number of successful executions (for steps)
- **Success Rate**: Percentage of successful executions (for steps)

## Statistics Explained

- **Mean**: The average duration across all runs
- **Median**: The middle value when durations are sorted (less affected by outliers)
- **P90**: 90% of runs completed within this duration (useful for SLA planning)
- **Success Rate**: Percentage of successful executions (for steps and jobs)

## Rerun Handling

The tool can handle workflow reruns in three ways:

1. **Include** (default): Process both original runs and reruns
2. **Exclude**: Only process original runs (`--exclude-reruns`)
3. **Only Reruns**: Only process reruns (`--only-reruns`)

Rerun statistics are displayed in the console output:
```
Total workflow runs found: 100
Workflow runs after filters: 85
  - Original runs: 70
  - Reruns: 15
```

## GitHub Token

You need a GitHub personal access token with the following permissions:
- `repo` (for private repositories)
- `public_repo` (for public repositories)
- `read:org` (if analyzing organization repositories)

Create a token at: https://github.com/settings/tokens

## Troubleshooting

### Authentication Errors

If you see `401` or `403` errors:
- Verify your token has the correct permissions
- Check that the token hasn't expired
- Ensure you have access to the repository

### No Data Found

If no workflow runs are found:
- Check the date range (workflows might be outside the specified range)
- Verify the workflow ID is correct
- Ensure the branch name matches exactly
- Check if filters are too restrictive (e.g., `--only-success` when all runs failed)

### Date Format Errors

Dates must be in `YYYY-MM-DD` format:
- ✅ Correct: `2024-01-01`
- ❌ Incorrect: `01/01/2024`, `2024-1-1`

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
Overall metrics exported to: gha_metrics.csv
Per-job metrics exported (including all steps):
  - gha_metrics_job_build.csv
  - gha_metrics_job_test.csv
  - gha_metrics_job_deploy.csv

Master and summary CSV files exported:
  - gha_master_metrics.csv (all metrics organized by category)
  - gha_jobs_summary.csv (jobs summary)
  - gha_steps_summary.csv (steps summary)
  - gha_success_rates_summary.csv (success rates summary)
============================================================
```

## Development

### Running Tests

```bash
./gradlew test
```

### Building

```bash
./gradlew build
```

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

