package lol.hexbench;

import lol.hexbench.data.CommunityDragonItemLoader;
import lol.hexbench.data.TftItemRepository;

import java.io.IOException;

public class AppContext {
    private final TftItemRepository itemRepository;

    public AppContext() throws IOException {
        CommunityDragonItemLoader itemLoader = new CommunityDragonItemLoader();
        this.itemRepository = new TftItemRepository(itemLoader.loadItems());
    }

    public TftItemRepository getItemRepository() {
        return itemRepository;
    }
}
