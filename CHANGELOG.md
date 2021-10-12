# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.3.0 - 2021-10-12
### Changed
- Always provide source and target revision for pull request [#23](https://github.com/scm-manager/scm-teamscale-plugin/pull/23)
- Notify teamscale if pull requests are merged or rejected [#23](https://github.com/scm-manager/scm-teamscale-plugin/pull/23)

## 1.2.0 - 2020-12-17
### Added
- Mark read only verbs to be able to see teamscale findings in archived repositories ([#8](https://github.com/scm-manager/scm-teamscale-plugin/pull/8))

## 1.1.0 - 2020-09-11
### Changed
- Set span kind for http requests (for Trace Monitor)

## 1.0.0 - 2020-06-04
### Added
- Increase permissions on teamscale permission role
- Add findings area in pull request view for teamscale findings
- Trigger Pull-Request-Updated-Event also on new commits on related branches

## 1.0.0-rc2 - 2020-05-08
### Added
- webhooks to notify teamscale about new /changed pull requests
- provide new pull request rest endpoint for teamscale

## 1.0.0-rc1

### Added
- global and repository specific configuration
- webhook to notify configured teamscale instance about new commit

### Changed
- Changeover to MIT license ([#1](https://github.com/scm-manager/scm-teamscale-plugin/pull/1))
