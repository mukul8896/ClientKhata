package dbBackup;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

public class BackupServices {

    Context context;

    public BackupServices(Context context){
        this.context=context;
    }
    public void requestSignIn(){
        GoogleSignInOptions signInOptions=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.DRIVE_FILE))
                .build();

        GoogleSignInClient client= GoogleSignIn.getClient(context,signInOptions);


    }
}
