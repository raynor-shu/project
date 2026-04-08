
// 获取主页面传递来的tabId
const urlParams = new URLSearchParams(window.location.search);
const tabId = parseInt(urlParams.get('tabId'));

// 加解密按钮操作：根据主页面tabId操作主页面
document.body.querySelector('#cryptoButton').onclick = function() {
  chrome.scripting.executeScript({
    target : { tabId: tabId },
    files : ['crypto-js.min.js', 'crypto.js']
  });	
};

// 解析获取key和iv：base64解码后与salt值异或
function parseKeyAndIv(keyAndIv) {
	var keyAndIvArray = CryptoJS.enc.Base64.parse(keyAndIv);
	var key = CryptoJS.enc.Base64.parse("AAAAAAAAAAAAAAAAAAAAAA==");
	var iv = CryptoJS.enc.Base64.parse("AAAAAAAAAAAAAAAAAAAAAA==");
	key.words[0] = keyAndIvArray.words[0] ^ 1810553260;
	key.words[1] = keyAndIvArray.words[1] ^ -1045778859;
	key.words[2] = keyAndIvArray.words[2] ^ -1517462908;
	key.words[3] = keyAndIvArray.words[3] ^ -1771775586;
	iv.words[0] = keyAndIvArray.words[4] ^ 287489218;
	iv.words[1] = keyAndIvArray.words[5] ^ 1310416102;
	iv.words[2] = keyAndIvArray.words[6] ^ -728006219;
	iv.words[3] = keyAndIvArray.words[7] ^ 588925637;

	return {"key": key, "iv": iv};
}

// 保存AES Key和Iv 按钮操作：解析并保存到本地存储
document.body.querySelector('#saveKeyAndIvAndPrefix').onclick = function() {
	// 读取文本框
	var settingStr = document.body.querySelector('#aesKeyAndIvText').value;
	var prefixStr = document.body.querySelector('#encryptPrefix').value;
	
	// 解析获取版本和对应密钥
	var settingBase64DecodeStr = CryptoJS.enc.Utf8.stringify(CryptoJS.enc.Base64.parse(settingStr)).replaceAll("'", "\"");
	var settingJson = JSON.parse(settingBase64DecodeStr);
	console.log(settingJson);
	var activeVersion = settingJson["activeVersion"];
	
	var parsedKeyAndIv = {};
	for (let ver in settingJson) {
		if (ver != "activeVersion") {
			parsedKeyAndIv[ver] = parseKeyAndIv(settingJson[ver]);
		}
	}
	
	// 保存本地存储
	chrome.storage.local.set({"apolloAesKeyAndIvA": settingStr, "apolloParsedKeyAndIvA": parsedKeyAndIv, "apolloActiveVersionA": activeVersion, "encryptPrefixA":prefixStr});
};

// 从本地存储获取keyAndIv字符串，并赋值到text输入框内
function getStorage() {
	chrome.storage.local.get(["apolloAesKeyAndIvA", "encryptPrefixA"]).then((storageKeyAndIvAndPrefix) => {
		var keyAndIv = storageKeyAndIvAndPrefix.apolloAesKeyAndIvA;
		console.log(keyAndIv);
		document.body.querySelector('#aesKeyAndIvText').value = keyAndIv;
		document.body.querySelector('#encryptPrefix').value = storageKeyAndIvAndPrefix.encryptPrefixA;
	});
}

getStorage();


