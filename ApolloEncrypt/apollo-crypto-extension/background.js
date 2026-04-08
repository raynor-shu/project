
// 点击操作打开边栏，并将tabId传递进去
chrome.action.onClicked.addListener((tab) => {
	chrome.sidePanel.setOptions({
    tabId: tab.id,
    path: 'sidepanel.html?tabId=' + tab.id,
    enabled: true
  });
  
	chrome.sidePanel.open({ 
		tabId: tab.id 
	});  
});
  
