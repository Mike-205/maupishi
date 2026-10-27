# DM Sans

- **Used in the app:** four static, non-italic files, copied into `app/src/main/res/font/` and renamed to valid Android resource names:

  | Source file | App resource |
  |---|---|
  | `DMSans-Regular.ttf` | `dm_sans_regular.ttf` |
  | `DMSans-Medium.ttf` | `dm_sans_medium.ttf` |
  | `DMSans-SemiBold.ttf` | `dm_sans_semibold.ttf` |
  | `DMSans-Bold.ttf` | `dm_sans_bold.ttf` |

- **Why static, not variable:** the four static files total about 225 KB and the variable file is about 239 KB, so there is no size saving. Static files work on every supported Android version.
- **Downloaded:** 2026-10-27, from the "DM Sans" family download (variable and static files).
- **Project:** https://github.com/googlefonts/dm-fonts (named in the licence and the download's README).
- **License:** SIL Open Font License 1.1. The full text is in `OFL.txt` next to this file. DM Sans must be listed on the credits screen before release.
