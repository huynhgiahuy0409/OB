package test.java.com.mservice.fs.onboarding.model.common.userinfo;

import com.mservice.fs.sof.queue.model.profile.FaceMatching;
import com.mservice.fs.sof.queue.model.profile.UserProfileInfo;
import com.mservice.fs.utils.JsonUtil;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author hoang.thai
 * on 12/27/2023
 */
public class FaceMatchingTest {

    public FaceMatchingTest() {
    }

    @Test //test
    public void testParseFaceMatchingEnum() throws IOException {
        String stringTest = "{\"faceMatching\":\"1\"}";
        UserProfileInfo userProfileInfo = JsonUtil.fromString(stringTest, UserProfileInfo.class);
        AssertJUnit.assertEquals(FaceMatching.MATCHED, userProfileInfo.getFaceMatching());
    }

    @Test
    public void testEnum() throws IOException {
        UserProfileInfo userProfileInfo = new UserProfileInfo();
        userProfileInfo.setFaceMatching(String.valueOf(FaceMatching.MATCHED.getCode()));
        String expected = "{\"nationalityKyc\":\"Việt Nam\",\"verifiedAccount\":false,\"faceMatching\":1}";
        String actual = JsonUtil.toString(userProfileInfo);
        AssertJUnit.assertEquals(expected, actual);
    }


}
