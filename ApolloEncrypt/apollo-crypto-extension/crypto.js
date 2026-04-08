

function tryCrypto(parsedKeyAndIv, ver, encryptPrefix) {
	
//	var key = CryptoJS.enc.Base64.parse('wNcNN8JiDAXhLxULDu9AQg==');
//	var iv = CryptoJS.enc.Base64.parse('toXD6Xac5nEGBfpHF07afg==');

	function encrypt(valueEditor) {
		console.log("尝试加密");
		var txt = CryptoJS.enc.Utf8.parse(valueEditor.value);
		// 获取当前有效版本的key和iv
		var keyAndIv = parsedKeyAndIv[ver];
		var key = keyAndIv["key"];
		var iv = keyAndIv["iv"];
		
		// 解密数据并更新到控件内
		var encryptTxt = CryptoJS.AES.encrypt(txt, key, {iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 });
		valueEditor.value = encryptPrefix + '.' + ver + '.' + encryptTxt.toString();
	}
	
	function decrypt(valueEditor) {
		console.log("尝试解密");
		var strWithVer = valueEditor.value.substring((encryptPrefix + '.').length);
		// 根据密文内的版本号获取对应版本key和iv
		var useVer = strWithVer.substring(0, strWithVer.indexOf("."));
		var keyAndIv = parsedKeyAndIv[useVer];
		var key = keyAndIv["key"];
		var iv = keyAndIv["iv"];
		
		// 加密数据并更新到控件内
		var encryptTxt = CryptoJS.enc.Base64.parse(strWithVer.substring((useVer + '.').length));
		var decryptTxt = CryptoJS.AES.decrypt({ciphertext: encryptTxt}, key, {iv: iv, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 }); 
		valueEditor.value = CryptoJS.enc.Utf8.stringify(decryptTxt);
	}

	// 从apollo界面html内获取指定输入框的控件
	var configInfoDiv = document.getElementById("config-info");
	var configItemContainerDiv = configInfoDiv.querySelectorAll('.config-item-container')[0];
	var itemModalForm = configItemContainerDiv.getElementsByTagName('form')[1];
	var modalDialogDiv = itemModalForm.querySelectorAll('.modal-dialog')[0];
	var modalContentDiv = itemModalForm.querySelectorAll('.modal-content')[0];
	var modalBodyDiv = modalContentDiv.querySelectorAll('.modal-body')[0];
	var formGroupDiv = modalBodyDiv.querySelectorAll('.form-group')[2];
	var valueEditor = formGroupDiv.getElementsByTagName("textarea")[0];
	
	// 根据控件内容判断加密或解密
	var txtValue = valueEditor.value;
	if (txtValue.startsWith(encryptPrefix + '.')) {
		decrypt(valueEditor);
	} else {
		encrypt(valueEditor);
	}
	
	// 刷新控件事件，以激活输入操作，更新相关的变量，否则 提交时 会用老值
	valueEditor.dispatchEvent(new Event('input', { bubbles: false }));
}

function getStorageAndCrypto() {
	// 从本地存储读取密钥表及当前有效版本，并尝试加解密
	chrome.storage.local.get(["apolloParsedKeyAndIvA", "apolloActiveVersionA", "encryptPrefixA"]).then((storageKeyAndIvAndPrefix) => {
		var parsedKeyAndIv = storageKeyAndIvAndPrefix.apolloParsedKeyAndIvA;
		var activeVersion = storageKeyAndIvAndPrefix.apolloActiveVersionA;
		var encryptPrefix = storageKeyAndIvAndPrefix.encryptPrefixA;
		
		tryCrypto(parsedKeyAndIv, activeVersion, encryptPrefix);
	});
}

getStorageAndCrypto();

