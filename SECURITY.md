# Security policy

## Supported versions

Security fixes are applied to the latest published release and the default branch. Older experimental builds may not receive backports.

## Reporting a vulnerability

Please do not open a public issue for a vulnerability, a report containing credentials or cookies, or a log that exposes private user data.

Use GitHub's **Report a vulnerability** option under the repository's Security tab when private vulnerability reporting is available. If that option is unavailable, contact the maintainer through the public contact method listed on the maintainer's GitHub profile and request a private reporting channel. Do not include exploit details or sensitive logs in the initial public message.

Include, when possible:

- affected version and commit
- affected Android version and device architecture
- concise reproduction steps or a proof of concept
- expected impact and attack prerequisites
- whether the issue is inherited from upstream PiliPlus or specific to this HDR fork
- suggested mitigation, if known

Reports will be acknowledged as maintainer availability permits. After validation, the maintainer will coordinate a fix and disclosure timeline with the reporter. Please allow time for users to update before publishing technical details.

## Scope

Relevant areas include native Android interfaces, media and URL handling, dependency vulnerabilities, release integrity, unsafe log exposure, EGL/OpenGL resource handling, and privilege or data-boundary violations. Visual artifacts, device incompatibility, and performance regressions without a security impact should use the normal bug-report template.
