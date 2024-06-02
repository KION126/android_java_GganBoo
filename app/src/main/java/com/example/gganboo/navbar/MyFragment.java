package com.example.gganboo.navbar;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.gganboo.R;
import com.example.gganboo.databinding.FragmentMyBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private @NonNull FragmentMyBinding binding;

    public MyFragment() {
        // Required empty public constructor
    }

    public static MyFragment newInstance(String param1, String param2) {
        MyFragment fragment = new MyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMyBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        database.getReference("Users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        binding.txtName.setText(snapshot.child("name").getValue(String.class));
                        binding.txtEmail.setText(snapshot.child("email").getValue(String.class));
                        String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                        loadProfileImage(imageUrl);
                    } else {
                        Log.d("firebase", "No data found");
                    }
                } else {
                    Log.d("firebase", "Error getting data", task.getException());
                }
            }
        });

        return view;
    }

    private void loadProfileImage(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_person_24)  // 로딩 중에 표시할 이미지
                .error(R.drawable.ic_person_24)        // 로드 실패 시 표시할 이미지
                .into(binding.imgProfileImage);
    }
}
