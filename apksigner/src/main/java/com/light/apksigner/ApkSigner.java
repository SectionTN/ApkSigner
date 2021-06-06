package com.light.apksigner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ApkSigner {
    private static final String[] META_INF_FILES_TO_SKIP_ENDINGS = new String[]{"manifest.mf", ".sf", ".rsa", ".dsa", ".ec"};
    private static final String HASHING_ALGORITHM = "SHA1";

    private RSAPrivateKey mPrivateKey;
    private File mTemplateFile;

    private String mSignerName = "CERT";

    public ApkSigner(String signerName) throws Exception {
		mSignerName = signerName;
        mPrivateKey = Utils.getPrivateKey();
    }
    
    public ApkSigner(File privateKey, File templateFile) throws Exception{
        Keystore.templateFile = Utils.readFile(templateFile);
        mPrivateKey = Utils.readPrivateKey(privateKey);
    }

    public void sign(File input, File output) throws Exception {
        sign(new FileInputStream(input), new FileOutputStream(output));
    }
	
	public void sign(String input, String output) throws Exception {
        sign(new FileInputStream(input), new FileOutputStream(output));
    }

    public void sign(InputStream apkInputStream, OutputStream output) throws Exception {
        ManifestBuilder manifest = new ManifestBuilder();
        SignatureFileGenerator signature = new SignatureFileGenerator(manifest, HASHING_ALGORITHM);

        ZipInputStream apkZipInputStream = new ZipInputStream(apkInputStream);
		
        ZipAlignZipOutputStream zipOutputStream = ZipAlignZipOutputStream.create(output, 4);
        MessageDigest messageDigest = MessageDigest.getInstance(HASHING_ALGORITHM);
        ZipEntry zipEntry;
        OUTER:
        while ((zipEntry = apkZipInputStream.getNextEntry()) != null) {
            if (zipEntry.isDirectory())
                continue;

            if(zipEntry.getName().toLowerCase().startsWith("meta-inf/")) {
                for(String fileToSkipEnding: META_INF_FILES_TO_SKIP_ENDINGS) {
                    if(zipEntry.getName().toLowerCase().endsWith(fileToSkipEnding))
                        continue OUTER;
                }
            }


            messageDigest.reset();
            DigestInputStream entryInputStream = new DigestInputStream(apkZipInputStream, messageDigest);

            ZipEntry newZipEntry = new ZipEntry(zipEntry.getName());
            newZipEntry.setMethod(zipEntry.getMethod());
            if (zipEntry.getMethod() == ZipEntry.STORED) {
                newZipEntry.setSize(zipEntry.getSize());
                newZipEntry.setCompressedSize(zipEntry.getSize());
                newZipEntry.setCrc(zipEntry.getCrc());
            }

            zipOutputStream.setAlignment(newZipEntry.getName().endsWith(".so") ? 4096 : 4);
            zipOutputStream.putNextEntry(newZipEntry);
            Utils.copyStream(entryInputStream, zipOutputStream);
            zipOutputStream.closeEntry();
            apkZipInputStream.closeEntry();

            ManifestBuilder.ManifestEntry manifestEntry = new ManifestBuilder.ManifestEntry();
            manifestEntry.setAttribute("Name", zipEntry.getName());
            manifestEntry.setAttribute(HASHING_ALGORITHM + "-Digest", Utils.base64Encode(messageDigest.digest()));
            manifest.addEntry(manifestEntry);
        }

        zipOutputStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        zipOutputStream.write(manifest.build().getBytes("UTF-8"));
        zipOutputStream.closeEntry();

        zipOutputStream.putNextEntry(new ZipEntry(String.format("META-INF/%s.SF", mSignerName)));
        zipOutputStream.write(signature.generate().getBytes("UTF-8"));
        zipOutputStream.closeEntry();

        zipOutputStream.putNextEntry(new ZipEntry(String.format("META-INF/%s.RSA", mSignerName)));
        zipOutputStream.write(Keystore.templateFile);
        zipOutputStream.write(Utils.sign(HASHING_ALGORITHM, mPrivateKey, signature.generate().getBytes("UTF-8")));
        zipOutputStream.closeEntry();

        apkZipInputStream.close();
        zipOutputStream.close();
    }


    /**
     * Sets name of the .SF and .RSA file in META-INF
     *
     * @param signerName desired .SF and .RSA files name
     */
    public void setSignerName(String signerName) {
        mSignerName = signerName;
    }
}
