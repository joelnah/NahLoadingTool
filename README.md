# NahLoadingTool

implementation 'com.github.joelnah:NahLoadingTool:1.0'



    public class MainActivity extends AppCompatActivity {

    private NProgress nProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void MainClick(View v){
        switch (v.getId()) {
            case R.id.indeterminate:
                nProgress = NProgress.create(this)
                        .setStyle(NProgress.Style.SPIN_INDETERMINATE);
                scheduleDismiss();
                break;
            case R.id.label_indeterminate:
                nProgress = NProgress.create(this)
                        .setStyle(NProgress.Style.SPIN_INDETERMINATE)
                        .setLabel("Please wait")
                        .setCancellable(true);
                scheduleDismiss();
                break;
            case R.id.detail_indeterminate:
                nProgress = NProgress.create(this)
                        .setStyle(NProgress.Style.SPIN_INDETERMINATE)
                        .setLabel("Please wait")
                        .setDetailsLabel("Downloading data");
                scheduleDismiss();
                break;
            case R.id.determinate:
                nProgress = NProgress.create(MainActivity.this)
                        .setStyle(NProgress.Style.PIE_DETERMINATE)
                        .setLabel("Please wait");
                simulateProgressUpdate();
                break;
            case R.id.annular_determinate:
                nProgress = NProgress.create(MainActivity.this)
                        .setStyle(NProgress.Style.ANNULAR_DETERMINATE)
                        .setLabel("Please wait");
                simulateProgressUpdate();
                break;
            case R.id.bar_determinate:
                nProgress = NProgress.create(MainActivity.this)
                        .setStyle(NProgress.Style.BAR_DETERMINATE)
                        .setLabel("Please wait")
                .setButtonMsg("Cancel", view -> {
                    if(handler!= null){
                        handler.removeCallbacks(postRun);
                    }
                    nProgress.dismiss();
                });
                simulateProgressUpdate();
                break;
            case R.id.custom_view:
                ImageView iv = new ImageView(this);
                iv.setImageResource(R.mipmap.ic_launcher);
                nProgress = NProgress.create(this)
                        .setCustomView(iv)
                        .setLabel("This is a custom view");
                scheduleDismiss();
                break;
            case R.id.custom_layout:
                nProgress = NProgress.create(this)
                        .setCustomLayout(R.layout.dialog_wheel)
                        .setLabel("This is a custom layout");
                scheduleDismiss();
                break;
            case R.id.dim_background:
                nProgress = NProgress.create(this)
                        .setStyle(NProgress.Style.SPIN_INDETERMINATE)
                        .setDimAmount(0.5f);
                scheduleDismiss();
                break;
            case R.id.custom_color_animate:
                //noinspection deprecation
                nProgress = NProgress.create(this)
                        .setStyle(NProgress.Style.SPIN_INDETERMINATE)
                        .setWindowColor(getResources().getColor(R.color.colorPrimary))
                        .setAnimationSpeed(2);
                scheduleDismiss();
                break;
        }

        nProgress.show();
    }

    Handler handler;
    Runnable postRun;
    private void simulateProgressUpdate() {
        nProgress.setMaxProgress(100);
        handler = new Handler();
        postRun = new Runnable() {
            int currentProgress;
            @Override
            public void run() {
                currentProgress += 1;
                nProgress.setProgress(currentProgress);
                if (currentProgress < 100) {
                    handler.postDelayed(this, 50);
                }
            }
        };
        handler.postDelayed(postRun, 100);
    }

    private void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nProgress.dismiss();
            }
        }, 2000);
    }
}
