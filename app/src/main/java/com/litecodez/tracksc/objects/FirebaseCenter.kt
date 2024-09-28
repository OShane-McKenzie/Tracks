package com.litecodez.tracksc.objects

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.lang.ref.WeakReference

object FirebaseCenter {
    private var dbReference: WeakReference<FirebaseFirestore>? = null
    private var dbAuthReference: WeakReference<FirebaseAuth>? = null
    private val storage = Firebase.storage

    fun getDatabase(): FirebaseFirestore {
        var db = dbReference?.get()

        if (db == null) {
            db = FirebaseFirestore.getInstance()
            dbReference = WeakReference(db)
        }

        return db
    }

    fun getAuth(): FirebaseAuth {
        var auth = dbAuthReference?.get()
        if (auth == null) {
            auth = FirebaseAuth.getInstance()
            dbAuthReference = WeakReference(auth)
        }

        return auth
    }

    fun getStorage(): StorageReference {
        return storage.reference
    }
}