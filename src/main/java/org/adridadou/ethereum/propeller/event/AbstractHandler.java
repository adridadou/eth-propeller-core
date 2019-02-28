package org.adridadou.ethereum.propeller.event;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by davidroon on 19.08.16.
 * This code is released under Apache 2 license
 */
public class AbstractHandler<T> implements ObservableOnSubscribe<T> {
    public final Observable<T> observable;
    private final Set<ObservableEmitter<? super T>> emitters = ConcurrentHashMap.newKeySet();

    public AbstractHandler() {
        observable = Observable.create(this);
    }

    @Override
    public void subscribe(ObservableEmitter<T> observableEmitter) throws Exception {
        emitters.add(observableEmitter);
        removeDisposed();
    }

    public void on(final T param) {
        newElement(param);
    }

    public void newElement(final T param) {
        removeDisposed();
        emitters.forEach(emitter -> {
            try {
                if (emitter.isDisposed()) {
                    emitter.onComplete();
                    emitters.remove(emitter);
                } else {
                    emitter.onNext(param);
                }
            } catch (Throwable ex) {
                emitter.onError(ex);
            }
        });
    }

    private void removeDisposed() {
        Set<ObservableEmitter<? super T>> disposed = emitters.stream()
                .filter(ObservableEmitter::isDisposed)
                .collect(Collectors.toSet());
        emitters.removeAll(disposed);
    }

}
