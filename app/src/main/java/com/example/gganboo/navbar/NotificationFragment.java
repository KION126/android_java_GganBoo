package com.example.gganboo.navbar;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.gganboo.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment {

    // Fragment 초기화 파라미터 (필요한 경우 이름을 변경)
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Fragment 파라미터 변수
    private String mParam1;
    private String mParam2;

    // 기본 생성자 (필수)
    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Fragment 초기화 메서드
     *
     * @param param1 첫 번째 파라미터
     * @param param2 두 번째 파라미터
     * @return NotificationFragment의 새 인스턴스
     */
    // 초기화 파라미터의 타입과 개수를 변경
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1); // ARG_PARAM1 값을 가져옴
            mParam2 = getArguments().getString(ARG_PARAM2); // ARG_PARAM2 값을 가져옴
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Fragment의 레이아웃을 인플레이트
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }
}
