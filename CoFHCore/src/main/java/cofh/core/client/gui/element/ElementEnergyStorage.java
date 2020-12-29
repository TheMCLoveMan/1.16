package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.core.energy.EnergyStorageCoFH;
import cofh.core.util.helpers.RenderHelper;

public class ElementEnergyStorage extends ElementResourceStorage {

    public ElementEnergyStorage(IGuiAccess gui, int posX, int posY, EnergyStorageCoFH storage) {

        super(gui, posX, posY, storage);
        clearable = () -> !storage.isCreative();
    }

    @Override
    protected void drawResource() {

        if (storage.isCreative() && creativeTexture != null) {
            RenderHelper.bindTexture(creativeTexture);
        } else {
            RenderHelper.bindTexture(texture);
        }
        int resourceHeight = height - 2;
        int amount = getScaled(resourceHeight);
        drawTexturedModalRect(posX(), posY() + 1 + resourceHeight - amount, width, 1 + resourceHeight - amount, width, amount);
    }

}
