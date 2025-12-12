Good point - commit count doesn't reflect actual code contribution. Here are ways to track code lines per time period for each contributor:

## Using Git Log with Time Ranges

**By specific date range:**
```bash
# Lines changed by each author in a date range
git log --since="2024-01-01" --until="2024-12-31" --format='%aN' | sort -u | while read name; do
  echo -en "$name\t";
  git log --author="$name" --since="2024-01-01" --until="2024-12-31" --pretty=tformat: --numstat | \
    awk '{ add += $1; subs += $2; loc += $1 - $2 } END \
    { printf "+%s -%s (net: %s)\n", add, subs, loc }';
done
```

**By month:**
```bash
# Monthly breakdown for a specific author
for month in {01..12}; do
  echo "2024-$month:";
  git log --author="username" --since="2024-$month-01" --until="2024-$month-31" --pretty=tformat: --numstat | \
    awk '{ add += $1; subs += $2 } END { printf "  +%s -%s\n", add, subs }';
done
```

**By week:**
```bash
# Last 12 weeks
for i in {0..11}; do
  start_date=$(date -d "$i weeks ago" +%Y-%m-%d);
  end_date=$(date -d "$(($i-1)) weeks ago" +%Y-%m-%d);
  echo "Week starting $start_date:";
  git log --all --since="$start_date" --until="$end_date" --format='%aN' | sort -u | while read name; do
    stats=$(git log --author="$name" --since="$start_date" --until="$end_date" --pretty=tformat: --numstat | \
      awk '{ add += $1; subs += $2 } END { printf "+%s -%s", add, subs }');
    echo "  $name: $stats";
  done;
done
```

## Python Script for Detailed Analysis

Here's a more flexible script that generates a CSV report:

```python
#!/usr/bin/env python3
import subprocess
import sys
from datetime import datetime, timedelta
from collections import defaultdict
import csv

def get_commit_stats(author, since_date, until_date):
    cmd = [
        'git', 'log',
        f'--author={author}',
        f'--since={since_date}',
        f'--until={until_date}',
        '--pretty=tformat:',
        '--numstat'
    ]
    
    result = subprocess.run(cmd, capture_output=True, text=True)
    
    added = 0
    removed = 0
    
    for line in result.stdout.strip().split('\n'):
        if line:
            parts = line.split('\t')
            if len(parts) >= 2 and parts[0].isdigit() and parts[1].isdigit():
                added += int(parts[0])
                removed += int(parts[1])
    
    return added, removed

def get_all_authors():
    cmd = ['git', 'log', '--format=%aN']
    result = subprocess.run(cmd, capture_output=True, text=True)
    return sorted(set(result.stdout.strip().split('\n')))

def generate_report(period='month', periods=12):
    authors = get_all_authors()
    
    # Prepare CSV
    with open('git_stats_report.csv', 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        
        # Header
        header = ['Author']
        dates = []
        
        for i in range(periods):
            if period == 'month':
                date = datetime.now() - timedelta(days=30*i)
                date_str = date.strftime('%Y-%m')
            elif period == 'week':
                date = datetime.now() - timedelta(weeks=i)
                date_str = date.strftime('%Y-W%U')
            else:  # day
                date = datetime.now() - timedelta(days=i)
                date_str = date.strftime('%Y-%m-%d')
            
            dates.append(date_str)
            header.extend([f'{date_str} Added', f'{date_str} Removed', f'{date_str} Net'])
        
        writer.writerow(header)
        
        # Data rows
        for author in authors:
            row = [author]
            
            for i in range(periods):
                if period == 'month':
                    until = datetime.now() - timedelta(days=30*i)
                    since = until - timedelta(days=30)
                elif period == 'week':
                    until = datetime.now() - timedelta(weeks=i)
                    since = until - timedelta(weeks=1)
                else:  # day
                    until = datetime.now() - timedelta(days=i)
                    since = until - timedelta(days=1)
                
                added, removed = get_commit_stats(
                    author,
                    since.strftime('%Y-%m-%d'),
                    until.strftime('%Y-%m-%d')
                )
                
                net = added - removed
                row.extend([added, removed, net])
            
            writer.writerow(row)
    
    print(f"Report generated: git_stats_report.csv")

if __name__ == '__main__':
    # Usage: python git_stats.py [month|week|day] [number_of_periods]
    period = sys.argv[1] if len(sys.argv) > 1 else 'month'
    periods = int(sys.argv[2]) if len(sys.argv) > 2 else 12
    
    generate_report(period, periods)
```

Save as `git_stats.py` and run:
```bash
python git_stats.py month 12  # Last 12 months
python git_stats.py week 8    # Last 8 weeks
```

## Using GitLab API for Remote Analysis

```python
#!/usr/bin/env python3
import requests
from datetime import datetime, timedelta
import csv

GITLAB_URL = 'https://gitlab.com'
PROJECT_ID = 'your_project_id'
PRIVATE_TOKEN = 'your_token'

def get_commits_in_range(since, until):
    url = f'{GITLAB_URL}/api/v4/projects/{PROJECT_ID}/repository/commits'
    headers = {'PRIVATE-TOKEN': PRIVATE_TOKEN}
    params = {
        'since': since,
        'until': until,
        'all': 'true',
        'per_page': 100
    }
    
    commits = []
    page = 1
    
    while True:
        params['page'] = page
        response = requests.get(url, headers=headers, params=params)
        data = response.json()
        
        if not data:
            break
            
        commits.extend(data)
        page += 1
    
    return commits

def get_commit_stats(commit_id):
    url = f'{GITLAB_URL}/api/v4/projects/{PROJECT_ID}/repository/commits/{commit_id}'
    headers = {'PRIVATE-TOKEN': PRIVATE_TOKEN}
    
    response = requests.get(url, headers=headers)
    data = response.json()
    
    return {
        'author': data.get('author_name'),
        'additions': data.get('stats', {}).get('additions', 0),
        'deletions': data.get('stats', {}).get('deletions', 0)
    }

# Generate monthly report
for i in range(12):
    until = datetime.now() - timedelta(days=30*i)
    since = until - timedelta(days=30)
    
    commits = get_commits_in_range(since.isoformat(), until.isoformat())
    
    author_stats = {}
    for commit in commits:
        stats = get_commit_stats(commit['id'])
        author = stats['author']
        
        if author not in author_stats:
            author_stats[author] = {'additions': 0, 'deletions': 0}
        
        author_stats[author]['additions'] += stats['additions']
        author_stats[author]['deletions'] += stats['deletions']
    
    print(f"\n{since.strftime('%Y-%m')} to {until.strftime('%Y-%m')}:")
    for author, stats in author_stats.items():
        print(f"  {author}: +{stats['additions']} -{stats['deletions']}")
```

The Python scripts will give you the most flexibility to analyze code contributions over different time periods. Which approach would work best for your needs?