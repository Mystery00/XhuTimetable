#!/usr/bin/env bash
brew install bsdiff jq

outputDir='app/build/outputs/apk/release'
cloudUrl='https://xgkb.api.mystery0.vip'

cd "$APPCENTER_OUTPUT_DIRECTORY"

outputMetadataJson="${outputDir}/output-metadata.json"
versionName=$(cat "$outputMetadataJson" | jq -r '.elements[0].versionName')
versionCode=$(cat "$outputMetadataJson" | jq -r '.elements[0].versionCode')
apkFile=$(cat "$outputMetadataJson" | jq -r '.elements[0].outputFile')

mkdir patch
# 拷贝新版本安装包到临时目录
cp $mappingFile "patch/new.apk"
cd patch
# 下载旧版本文件
echo "开始下载旧版本：${LAST_VERSION_NAME} - ${LAST_VERSION_CODE}"
curl -# -o "old.apk" "$OLD_APK_URL"
patchFile="${LAST_VERSION_CODE}-${versionCode}.patch"
echo "生成拆分文件"
bsdiff old.apk new.apk $patchFile

apkMd5=$(md5sum "new.apk" | cut -d" " -f1)
patchMd5=$(md5sum "$patchFile" | cut -d" " -f1)

echo "=========================================="
echo "旧版本：${LAST_VERSION_NAME}-${LAST_VERSION_CODE}"
ls -l "old.apk"
echo "=========================================="
echo "新版本：${versionName}-${versionCode}"
ls -l "new.apk"
echo "md5: $apkMd5"
echo "=========================================="
echo "增量包：${patchFile}"
ls -l "$patchFile"
echo "md5: $patchMd5"
echo "=========================================="

echo

function uploadFile() {
  cd "$rootDir"
  file="$1"
  title="$2"
  mimeType="$3"
  # 申请签名
  signatureUrl="${cloudUrl}/api/rest/xhu-timetable/devops/signature"
  apkFileSize=$(wc -c <"$file")
  json='{"serviceName":"","storeType":"apk",fileSize:$fileSize,mimeType:$mimeType,title:$title}'
  requestJson=$(
    jq \
      --arg fileSize "$apkFileSize" \
      --arg title "$title" \
      --arg mimeType "$mimeType" \
      -n "$json"
  )
  apkSignResp=$(
    curl -s -X POST "$signatureUrl" \
      -H 'Content-Type: application/json' \
      -d "$requestJson"
  )
  uploadUrl=$(echo "$apkSignResp" | jq -r '.uploadUrl')
  key=$(echo "$apkSignResp" | jq -r '.uploadMeta.key')
  token=$(echo "$apkSignResp" | jq -r '.uploadMeta.signature')
  uploadResp=$(
    curl -s -X POST "$uploadUrl" \
      -F "key=$key" \
      -F "token=$token" \
      -F "file=@$file"
  )
  echo "$uploadResp" | jq -r '.resourceId'
}

apkResourceId=$(uploadFile "patch/new.apk" "${versionName}-${versionCode}.apk" "application/vnd.android.package-archive")
patchResourceId=$(uploadFile "patch/$patchFile" "$patchFile" "text/x-diff")

json='{apkResourceId:$apkResourceId,apkMd5:$apkMd5,patchResourceId:$patchResourceId,patchMd5:$patchMd5,updateLog:$updateLog,versionCode:$versionCode,versionName:$versionName,lastVersionCode:$lastVersionCode,forceUpdate:$forceUpdate,betaVersion:$betaVersion}'
requestJson=$(
  jq \
    --arg apkResourceId "$apkResourceId" \
    --arg apkMd5 "$apkMd5" \
    --arg patchResourceId "$patchResourceId" \
    --arg patchMd5 "$patchMd5" \
    --arg updateLog "$UPDATE_LOG" \
    --arg versionCode "$versionCode" \
    --arg versionName "$versionName" \
    --arg lastVersionCode "$LAST_VERSION_CODE" \
    --arg forceUpdate "$FORCE_UPDATE" \
    --arg betaVersion "$BETA" \
    -n "$json"
)
newVersionUrl="${cloudUrl}/api/rest/xhu-timetable/devops/version"
curl -s -X POST "$newVersionUrl" \
  -H 'Content-Type: application/json' \
  -d "$requestJson"
