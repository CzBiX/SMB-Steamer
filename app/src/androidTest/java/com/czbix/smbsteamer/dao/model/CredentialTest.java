package com.czbix.smbsteamer.dao.model;

import android.os.Parcel;
import android.test.AndroidTestCase;

public class CredentialTest extends AndroidTestCase {
    public void testParcelable1() throws Exception {
        final Parcel parcel = Parcel.obtain();
        Credential.ANONYMOUS.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        final Credential credential = Credential.CREATOR.createFromParcel(parcel);

        assertEquals(Credential.ANONYMOUS, credential);
    }

    public void testParcelable2() throws Exception {
        final Parcel parcel = Parcel.obtain();
        final Credential credential1 = new PasswordCredential("domain", "czbix", "123456");
        credential1.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);
        final Credential credential2 = Credential.CREATOR.createFromParcel(parcel);

        assertFalse(credential2.isAnonymous());
        assertEquals(credential1, credential2);
    }
}