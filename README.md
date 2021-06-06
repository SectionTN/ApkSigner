## Description
ApkSigner is a copy of [PseudoApkSigner](https://github.com/Aefyr/PseudoApkSigner). But it has functionality to sign an app without private key file and template file.
## How it works
It works just like [PseudoApkSigner](https://github.com/Aefyr/PseudoApkSigner). But this version of **ApkSigner** also can sign apps with stored bytes of private key file and template file. This is the trick to sign an app without private key and template.
## Adding to Project
Add this line to module-level build.gradle dependencies:
```nginx
implementation 'com.github.ratul-learner:apksigner:1.0'  
```
## Usage
You can use it with private key file and template file in the same way like PseudoApkSigner.
```java
new ApkSigner(new File("PrivateKeyFile"), new File("TemplateFile")).sign("UnsignedInputApkPath", "SignedOutputApkPath");  
```
But if you want to use it without any private key file and template file then use this code to create a new ApkSigner instance
```java
ApkSigner apkSigner = new ApkSigner("SignerNameWithoutSpace");  
```
Once you created an instance, just call it like this to sign an apk file
```java
apkSigner.sign("UnsignedInputApkPath", "SignedOutputApkPath");  
```
