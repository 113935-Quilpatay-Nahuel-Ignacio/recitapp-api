package com.recitapp.recitapp_api.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebaseUserService {

    private final FirebaseAuth firebaseAuth;

    public UserRecord createUserWithRole(String email, String password, String role) throws Exception {
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(email)
                .setPassword(password)
                .setEmailVerified(false);

        UserRecord userRecord = firebaseAuth.createUser(request);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        firebaseAuth.setCustomUserClaims(userRecord.getUid(), claims);

        return userRecord;
    }

    public void updateUserRole(String uid, String role) throws Exception {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        firebaseAuth.setCustomUserClaims(uid, claims);
    }

    public UserRecord getUser(String uid) throws Exception {
        return firebaseAuth.getUser(uid);
    }
}