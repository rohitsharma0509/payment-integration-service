package com.scb.paymentintegration.service.impl;

import com.scb.paymentintegration.exception.CryptoException;
import com.scb.paymentintegration.service.CryptoService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPObjectFactory;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Iterator;
import java.util.Objects;

@Slf4j
@Service
public class CryptoServiceImpl implements CryptoService {

    private static final String PROVIDER = "BC";

    @Override
    public File encrypt(File s1HashFile, String publicKey, String encryptedFileName, boolean withIntegrityCheck) {
        File encFile = new File(encryptedFileName);
        try (
                OutputStream fos = new FileOutputStream(encFile);
                OutputStream bos = new BufferedOutputStream(fos);
                ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        ) {
            PGPPublicKey encKey = readPublicKey(publicKey);

            Security.addProvider(new BouncyCastleProvider());
            PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(CompressionAlgorithmTags.ZIP);
            PGPUtil.writeFileToLiteralData(comData.open(bOut), PGPLiteralData.BINARY, s1HashFile);
            comData.close();
            PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(new JcePGPDataEncryptorBuilder(SymmetricKeyAlgorithmTags.CAST5)
                    .setWithIntegrityPacket(withIntegrityCheck).setSecureRandom(new SecureRandom()).setProvider(PROVIDER));
            cPk.addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(encKey).setProvider(PROVIDER));
            byte[] bytes = bOut.toByteArray();
            OutputStream cOut = cPk.open(bos, bytes.length);
            cOut.write(bytes);
            cOut.close();
        } catch (Exception e) {
            throw new CryptoException("Exception while encrypting file : ", e);
        }
        return encFile;
    }

    @Override
    public InputStream decrypt(InputStream encryptedFileStream, String keyFileName, char[] passwd) {
        log.info("file decryption started..");
        Resource resource = new ClassPathResource(keyFileName);
        try {
            InputStream privateKeyStream = new BufferedInputStream(resource.getInputStream());
            InputStream decodedStream = PGPUtil.getDecoderStream(encryptedFileStream);
            Security.addProvider(new BouncyCastleProvider());
            JcaPGPObjectFactory pgpF = new JcaPGPObjectFactory(decodedStream);
            Object o = pgpF.nextObject();

            PGPEncryptedDataList enc;
            if (o instanceof PGPEncryptedDataList) {
                enc = (PGPEncryptedDataList) o;
            } else {
                enc = (PGPEncryptedDataList) pgpF.nextObject();
            }

            if(Objects.isNull(enc)) {
                throw new CryptoException("encrypted data is null");
            }
            Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
            PGPPrivateKey sKey = null;
            PGPPublicKeyEncryptedData pbe = null;
            PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(privateKeyStream), new JcaKeyFingerprintCalculator());

            while (sKey == null && it.hasNext()) {
                pbe = it.next();
                sKey = findSecretKey(pgpSec, pbe.getKeyID(), passwd);
            }

            if (sKey == null) {
                throw new CryptoException("secret key for message not found.");
            }

            InputStream clear = pbe.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider(PROVIDER).build(sKey));
            JcaPGPObjectFactory plainFact = new JcaPGPObjectFactory(clear);
            PGPCompressedData cData = (PGPCompressedData) plainFact.nextObject();
            if(Objects.isNull(cData)) {
                throw new CryptoException("decrypted compressed data is null");
            }
            InputStream compressedStream = new BufferedInputStream(cData.getDataStream());
            JcaPGPObjectFactory pgpFact = new JcaPGPObjectFactory(compressedStream);
            Object message = pgpFact.nextObject();

            if (message instanceof PGPLiteralData) {
                PGPLiteralData ld = (PGPLiteralData) message;
                return ld.getInputStream();
            } else if (message instanceof PGPOnePassSignatureList) {
                throw new CryptoException("encrypted message contains a signed message - not literal data.");
            } else {
                throw new CryptoException("message is not a simple encrypted file - type unknown.");
            }
        } catch (PGPException | IOException e) {
            throw new CryptoException("Exception while decrypting file: ", e);
        }
    }

    private PGPPublicKey readPublicKey(String publicKeyFileName) throws IOException, PGPException {
        Resource resource = new ClassPathResource(publicKeyFileName);
        InputStream keyIn = new BufferedInputStream(resource.getInputStream());
        PGPPublicKey pubKey = readPublicKey(keyIn);
        keyIn.close();
        return pubKey;
    }

    /**
     * A simple routine that opens a key ring file and loads the first available key
     * suitable for encryption.
     *
     * @param input data stream containing the public key data
     * @return the first public key found.
     * @throws IOException
     * @throws PGPException
     */
    static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
        PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input), new JcaKeyFingerprintCalculator());
        Iterator<PGPPublicKeyRing> keyRingIter = pgpPub.getKeyRings();
        while (keyRingIter.hasNext()) {
            PGPPublicKeyRing keyRing = keyRingIter.next();

            Iterator<PGPPublicKey> keyIter = keyRing.getPublicKeys();
            while (keyIter.hasNext()) {
                PGPPublicKey key = keyIter.next();

                if (key.isEncryptionKey()) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException("Can't find encryption key in key ring.");
    }

    /**
     * Search a secret key ring collection for a secret key corresponding to keyID if it
     * exists.
     *
     * @param pgpSec a secret key ring collection.
     * @param keyID  keyID we want.
     * @param pass   passphrase to decrypt secret key with.
     * @return the private key.
     * @throws PGPException
     */
    static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws PGPException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null) {
            return null;
        }

        return pgpSecKey.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider(PROVIDER).build(pass));
    }
}
