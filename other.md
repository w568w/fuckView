### AppRulesUtils.java

1. 修改文件权限
2. 保存和读取文件
3. 保存规则文件的时候会把权限设置好

### MyApplication.java
1. 启动的时候去设置文件夹权限;
2. 华为手机重启之后，还需要把所有文件规则都要重新设置权限;
```
    if(isOpenSharedFile) {
        AppRulesUtils.setFilePermissions(getApplicationInfo().dataDir, 0757, -1, -1);
        AppRulesUtils.setFilePermissions(getDir(AppRulesUtils.RULES_DIR, Context.MODE_PRIVATE), 0777, -1, -1);
    }
```
