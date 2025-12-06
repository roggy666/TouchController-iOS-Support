package top.fifthlight.touchcontroller;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import top.fifthlight.touchcontroller.buildinfo.BuildInfo;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name(BuildInfo.MOD_NAME)
public class TouchControllerCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                TouchControllerTransformer.class.getName(),
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
