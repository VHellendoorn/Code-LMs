package com.bugaugaoshu.security.service.impl;

import com.bugaugaoshu.security.exception.VerifyFailedException;
import com.bugaugaoshu.security.repository.VerifyCodeRepository;
import com.bugaugaoshu.security.service.GenerateImageService;
import com.bugaugaoshu.security.service.SendMessageService;
import com.bugaugaoshu.security.service.VerifyCodeService;
import com.bugaugaoshu.security.util.VerifyCodeUtil;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.security.SecureRandom;
import java.util.Objects;


/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2019-11-26 17:07
 * 验证码服务
 */
@Service
public class DigitsVerifyCodeServiceImpl implements VerifyCodeService {
    private final VerifyCodeRepository verifyCodeRepository;

    private final GenerateImageService generateImageService;

    private final SendMessageService sendMessageService;

    private final VerifyCodeUtil verifyCodeUtil;

    private static final long VERIFY_CODE_EXPIRE_TIMEOUT = 60000L;

    public DigitsVerifyCodeServiceImpl(VerifyCodeRepository verifyCodeRepository, GenerateImageService generateImageService, SendMessageService sendMessageService, VerifyCodeUtil verifyCodeUtil) {
        this.verifyCodeRepository = verifyCodeRepository;
        this.generateImageService = generateImageService;
        this.sendMessageService = sendMessageService;
        this.verifyCodeUtil = verifyCodeUtil;
    }

    private static String randomDigitString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        return stringBuilder.toString();
    }

    private static String appendTimestamp(String string) {
        return string + "#" + System.currentTimeMillis();
    }

    @Override
    public void send(String key) {
        String verifyCode = randomDigitString(verifyCodeUtil.getLen());
        String verifyCodeWithTimestamp = appendTimestamp(verifyCode);
        verifyCodeRepository.save(key, verifyCodeWithTimestamp);
        sendMessageService.send(key, verifyCode);
    }

    @Override
    public void verify(String key, String code) {
        String lastVerifyCodeWithTimestamp = verifyCodeRepository.find(key);
        // 如果没有验证码，则随机生成一个
        if (lastVerifyCodeWithTimestamp == null) {
            lastVerifyCodeWithTimestamp = appendTimestamp(randomDigitString(verifyCodeUtil.getLen()));
        }
        String[] lastVerifyCodeAndTimestamp = lastVerifyCodeWithTimestamp.split("#");
        String lastVerifyCode = lastVerifyCodeAndTimestamp[0];
        long timestamp = Long.parseLong(lastVerifyCodeAndTimestamp[1]);
        if (timestamp + VERIFY_CODE_EXPIRE_TIMEOUT < System.currentTimeMillis()) {
            throw new VerifyFailedException("验证码已过期！");
        } else if (!Objects.equals(code, lastVerifyCode)) {
            throw new VerifyFailedException("验证码错误！");
        }
    }

    @Override
    public Image image(String key) {
        String verifyCode = randomDigitString(verifyCodeUtil.getLen());
        String verifyCodeWithTimestamp = appendTimestamp(verifyCode);
        Image image = generateImageService.imageWithDisturb(verifyCode);
        verifyCodeRepository.save(key, verifyCodeWithTimestamp);
        return image;
    }
}
