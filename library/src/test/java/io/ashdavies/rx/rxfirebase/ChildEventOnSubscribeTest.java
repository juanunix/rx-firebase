package io.ashdavies.rx.rxfirebase;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.reactivex.FlowableEmitter;
import io.reactivex.functions.Cancellable;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class ChildEventOnSubscribeTest {

  private ChildEventOnSubscribe onSubscribe;

  @Captor ArgumentCaptor<ChildEventListener> captor;

  @Mock Query query;
  @Mock FlowableEmitter<ChildEvent> emitter;
  @Mock DataSnapshot snapshot;

  @Before
  public void setUp() throws Exception {
    onSubscribe = new ChildEventOnSubscribe(query);
  }

  @Test
  public void shouldAddChildEventListener() throws Exception {
    onSubscribe.subscribe(emitter);
    then(query).should().addChildEventListener(captor.capture());

    captor.getValue().onChildAdded(snapshot, null);

    ArgumentCaptor<ChildEvent> captor = forClass(ChildEvent.class);
    then(emitter).should().onNext(captor.capture());

    ChildEvent event = captor.getValue();
    assertThat(event.snapshot()).isEqualTo(snapshot);
    assertThat(event.type()).isEqualTo(ChildEvent.Type.CHILD_ADDED);
  }

  @Test
  public void shouldRemoveChildEventListenerOnCancel() throws Exception {
    ArgumentCaptor<Cancellable> captor = forClass(Cancellable.class);

    onSubscribe.subscribe(emitter);
    then(emitter).should().setCancellable(captor.capture());
    then(query).should().addChildEventListener(this.captor.capture());

    captor.getValue().cancel();
    then(query).should().removeEventListener(this.captor.getValue());
  }
}
