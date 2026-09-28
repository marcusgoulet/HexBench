package lol.hexbench.data;

import lol.hexbench.model.TftItem;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityDragonItemLoaderTest {
    @Test
    void enrichesCurrentItemsFromFallbackDescriptionSources() throws IOException {
        CommunityDragonItemLoader loader = new CommunityDragonItemLoader();
        List<TftItem> items = loader.loadItems();

        TftItem adaptiveHelm = items.stream()
                .filter(item -> item.getId().equals("DA_AdaptiveHelm"))
                .findFirst()
                .orElseThrow();

        assertEquals("Adaptive Helm", adaptiveHelm.getName());
        assertEquals("TFT_Item_AdaptiveHelm", adaptiveHelm.getDescriptionSourceId());
        assertTrue(adaptiveHelm.getDescription().contains("15% Mana"));
        assertEquals(20.0, adaptiveHelm.getStats().get("Magic Resist"));
        assertEquals(3.0, adaptiveHelm.getStats().get("Mana Regen"));
        assertFalse(adaptiveHelm.getEffects().isEmpty());

        TftItem guinsoosRageblade = items.stream()
                .filter(item -> item.getId().equals("DA_GuinsoosRageblade"))
                .findFirst()
                .orElseThrow();

        assertEquals(10.0, guinsoosRageblade.getStats().get("Ability Power"));
        assertEquals(10.0, guinsoosRageblade.getStats().get("Attack Speed"));

        TftItem bfSword = items.stream()
                .filter(item -> item.getId().equals("DA_Component_BFSword"))
                .findFirst()
                .orElseThrow();

        assertEquals("TFT_Item_BFSword", bfSword.getDescriptionSourceId());
        assertEquals(10.0, bfSword.getStats().get("Attack Damage"));
        assertTrue(bfSword.getDescription().isBlank());

        TftItem spatula = items.stream()
                .filter(item -> item.getId().equals("DA_Component_Spatula"))
                .findFirst()
                .orElseThrow();

        assertTrue(spatula.getStats().isEmpty());
    }
}
