package com.aeciosantos.sitegen;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.google.common.eventbus.EventBus;
import com.sun.nio.file.SensitivityWatchEventModifier;

public class FsWatcher extends Thread {
    
    EventBus events;
    private Path fsPath;
    
    public FsWatcher(EventBus events, Path pagesPath) {
        this.events = events;
        this.fsPath = pagesPath;
    }
    
    public void run()  {
        try(WatchService service = fsPath.getFileSystem().newWatchService()) {
            fsPath.register(service, ENTRY_MODIFY);
            fsPath.register(service, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY}, SensitivityWatchEventModifier.HIGH);
            while(true) {
                WatchKey key = service.take();
                for(WatchEvent<?> event : key.pollEvents()) {
                    Kind<?> kind = event.kind();
                    if(ENTRY_MODIFY == kind) {
                        events.post(new FileModifiedEvent());
                    }
                }
                if(!key.reset()) {
                    break; //loop
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to monitor path: "+fsPath, e);
        }
    }
    
    static class FileModifiedEvent {
    }
    
}
