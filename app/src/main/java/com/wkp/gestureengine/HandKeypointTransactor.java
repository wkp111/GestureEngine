package com.wkp.gestureengine;

import android.util.SparseArray;
import com.huawei.hms.mlsdk.common.MLAnalyzer;
import com.huawei.hms.mlsdk.gesture.MLGesture;

public class HandKeypointTransactor implements MLAnalyzer.MLTransactor<MLGesture> {

    private FloatingVideoService mVideoService;

    public HandKeypointTransactor(FloatingVideoService videoService) {
        mVideoService = videoService;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void transactResult(MLAnalyzer.Result<MLGesture> result) {
        SparseArray<MLGesture> analyseList = result.getAnalyseList();
        for (int i = 0; i < analyseList.size(); i++) {
            MLGesture mlGesture = analyseList.get(i);
            if (mlGesture.getCategory() == MLGesture.FIST) {
                if (mVideoService != null) {
                    mVideoService.clickSpace();
                }
            }
        }
    }
}
