package com.wakatime.android.support.presenter;

/**
 * @author Joao Pedro Evangelista
 */

public interface WithBinding<V> {

    void bind(V viewModel);

    void unbind();
}
