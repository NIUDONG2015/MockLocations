package com.lulee007.mocklocations.ui.views;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.lulee007.mocklocations.R;
import com.lulee007.mocklocations.util.RxBus;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * User: lulee007@live.com
 * Date: 2016-02-24
 * Time: 10:49
 * 控制在{@link com.baidu.mapapi.map.BaiduMap}上绘制轨迹工具面板
 * <p>并将绘制状态信息 {@link com.lulee007.mocklocations.ui.views.DrawPanelView.DrawMode} 通过 {@link RxBus}发送到
 * <p>{@link com.lulee007.mocklocations.util.DrawTool}
 */
public class DrawPanelView {

    @Bind(R.id.btn_begin_or_cancel_draw)
    Button btnBeginOrCancelDraw;
    @Bind(R.id.btn_pan_map)
    Button btnPanMap;
    @Bind(R.id.btn_complete_draw)
    Button btnCompleteDraw;
    @Bind(R.id.btn_save)
    Button btnSave;

    public View getDrawPanelView() {
        return drawPanelView;
    }

    private View drawPanelView;
    private DrawMode drawMode;

    public boolean isInEditMode() {
        return drawMode == DrawMode.STARTED
                || drawMode == DrawMode.PANNING
                || drawMode == DrawMode.TO_PAN
                || drawMode == DrawMode.COMPLETED;
    }

    public void cancelEdit() {
        btnBeginOrCancelDraw.performClick();
    }


    public enum DrawMode {
        TO_START,
        STARTED,//btn begin or cancel
        PANNING,
        TO_PAN,
        SAVE,
        COMPLETED
    }

    public DrawPanelView(@NonNull ViewGroup root) {

        drawPanelView = LayoutInflater.from(root.getContext()).inflate(R.layout.draw_panel, root, false);
        ButterKnife.bind(this, drawPanelView);

        drawMode = DrawMode.TO_START;
        drawModeChanged(drawMode);
//        RxView.enabled(btnCompleteDraweDraw)enabled
    }

    @OnClick({R.id.btn_begin_or_cancel_draw, R.id.btn_pan_map, R.id.btn_complete_draw, R.id.btn_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_begin_or_cancel_draw:
                drawMode = drawMode == DrawMode.TO_START || drawMode == DrawMode.SAVE ? DrawMode.STARTED : DrawMode.TO_START;
                drawModeChanged(drawMode);
                break;
            case R.id.btn_pan_map:
                drawMode = drawMode == DrawMode.PANNING ? DrawMode.TO_PAN : DrawMode.PANNING;
                drawModeChanged(drawMode);
                break;
            case R.id.btn_complete_draw:
                drawModeChanged(drawMode = DrawMode.COMPLETED);
                break;
            case R.id.btn_save:
                drawModeChanged(drawMode = DrawMode.SAVE);
                break;
        }
    }

    private void drawModeChanged(DrawMode newMode) {
        btnBeginOrCancelDraw.setEnabled(false);
        btnCompleteDraw.setEnabled(false);
        btnPanMap.setEnabled(false);
        btnSave.setEnabled(false);
        boolean canPanMap = false;
        switch (newMode) {
            case PANNING://正在移动地图，不能绘制轨迹
                btnPanMap.setText("停止");
                btnPanMap.setEnabled(true);
                canPanMap = true;
                break;
            case TO_PAN://切换到绘制模式，不能移动地图
                btnPanMap.setText("移动");
                btnPanMap.setEnabled(true);
                btnBeginOrCancelDraw.setEnabled(true);
                btnCompleteDraw.setEnabled(true);
                break;
            case TO_START://取消了绘制，可以移动地图
                btnBeginOrCancelDraw.setText("开始");
                btnBeginOrCancelDraw.setEnabled(true);
                canPanMap = true;
                break;
            case STARTED://进入绘制模式，不能移动地图
                btnBeginOrCancelDraw.setText("取消");
                btnBeginOrCancelDraw.setEnabled(true);
                btnPanMap.setEnabled(true);
                btnCompleteDraw.setEnabled(true);
                break;
            case COMPLETED://完成了绘制，可以移动地图
                btnSave.setEnabled(true);
                btnBeginOrCancelDraw.setText("取消");
                btnBeginOrCancelDraw.setEnabled(true);
                canPanMap = true;
                break;
            case SAVE://进行保存轨迹，可以移动地图，切换为待绘制模式
                btnBeginOrCancelDraw.setEnabled(true);
                btnBeginOrCancelDraw.setText("开始");
                btnSave.setEnabled(true);
                canPanMap = true;
                break;
            default:
                break;
        }
        RxBus.getDefault().send(new DrawActionEvent(drawMode, canPanMap));

    }

    public class MapPanEvent {

        public boolean isPanEnabled() {
            return isPanEnabled;
        }

        private boolean isPanEnabled;


        public MapPanEvent(boolean isPanEnabled) {

            this.isPanEnabled = isPanEnabled;
        }
    }

    public class DrawActionEvent {
        private DrawMode drawMode;
        private boolean canPanMap;

        public DrawActionEvent(DrawMode drawMode, boolean canPanMap) {

            this.drawMode = drawMode;
            this.canPanMap = canPanMap;
        }

        public DrawMode getDrawMode() {
            return drawMode;
        }


        public boolean isCanPanMap() {
            return canPanMap;
        }


    }
}
