# Git Branching and Merging Guide

## 1. Branch Management
- `git branch <branch-name>` creates a new branch.
- `git checkout -b <branch-name>` or `git switch -c <branch-name>` creates and switches to a branch.

## 2. Merging & Conflict Resolution
- `git merge <branch-name>` integrates changes into current branch.
- Fast-forward merge occurs when no divergent commits exist.
- Merge conflicts arise when changes overlap on the same lines. Resolve conflicts manually, stage, and commit.
