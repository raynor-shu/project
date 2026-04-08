1. 说明
   借助apollo提供的spi加载支持，覆盖spi配置内com.ctrip.framework.apollo.internals.Injector的内容，
   指向自定义的EncryptInjector，然后按需加载自定义的EncryptConfigFactory和EncryptConfig完成解密拦截。
   使用chrome插件对apollo后台参数内容进行加解密操作
    
2. 自定义修改点
   注意，示例使用的apollo版本为1.7.0，如使用其他版本，需自行拷贝对应原生实现类再针对修改；
   EncryptInjector：拷贝自DefaultInjector，第57行修改为加载自定义的EncryptConfigFactory；
   EncryptConfigFactory：拷贝自DefaultConfigFactory，第30、32行修改为加载自定义的EncryptConfig；
   EncryptConfig：拷贝自DefaultConfig，updateConfig方法内增加加密密钥初始化和参数解密拦截两段逻辑；
   ApolloCrypto：加密密钥初始化及加解密实现类，加密密钥从环境变量读取，key为APOLLO_ENCRYPT_AESKEYANDIV
                 密钥格式为 {'activeVersion':'v01','v01':'xxxxx','v02':'xxxx'}，然后进行base64转换
                 单个密钥为32byte数字进行base64转换后的字符串，其中前16byte为AES的key，后16byte为iv
                 注意加密密钥解析后用盐值数组进行了异或操作，以避免使用密钥可直接手工解密的风险，该数组项的值请自行修改；

3. apollo后台加密chrome插件（apollo-crypto-extension目录）
   manifest.json：chrome插件定义
   background.js：加载插件边栏界面sidepanel.html
   sidepanel.html：参数输入框和按钮
                   aesKeyAndIvText输入框，环境变量内的加密密钥字符串
                   encryptPrefix输入框，加密前缀，ApolloCrypto代码内定义为 $ENCRYPT$，如有需要可自行修改
                   saveKeyAndIvAndPrefix按钮，将前面两个输入框内容保存在本地存储
                   cryptoButton按钮，加解密操作，根据内容是否以加密前缀开头决定解密还是加密
   setting.js：sidepanel的加载和按钮相关逻辑，注意如果项目内加密密钥盐值项有修改，则parseKeyAndIv方法内异或值需自行计算修改
   crypto.js：点击加解密按钮时，调用主界面脚本执行，查找apollo后台界面的编辑窗口，获取内容后执行加解密操作
              需点开apollo后台界面参数的修改按钮，弹出 修改配置项 小窗口时操作进行插件修改，修改完成后点击 提交
              如因apollo后台版本不同无法使用，可能需要自行查看对应html代码，修改逻辑查找到相应的输入框
                    
4. 测试
   TestController：程序运行后使用 curl http://localhost:16001/test/query?key=testkey 查询监听的 apollo-encrypt
                   应用application空间内 testkey 解密后的值