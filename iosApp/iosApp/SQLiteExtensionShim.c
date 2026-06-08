#include <sqlite3.h>

// iOS 系统 libsqlite3 不导出该符号；AndroidX SQLite cinterop 会引用但项目不会调用。
int sqlite3_load_extension(
    sqlite3 *db,
    const char *zFile,
    const char *zProc,
    char **pzErrMsg
) {
    (void)db;
    (void)zFile;
    (void)zProc;
    if (pzErrMsg != 0) {
        *pzErrMsg = 0;
    }
    return SQLITE_ERROR;
}
