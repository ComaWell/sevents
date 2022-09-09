package art.comacreates.sevents;

import java.util.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ListenerMap {

    private static final Listeners EMPTY_BAKED = Listeners.of(new Listener[0]);

    static final class Node {

        final Node parent;
        final Event.Type type;
        final List<Listener> listeners = new ArrayList<>();
        Map<Event, Node> children;

        Listeners baked = EMPTY_BAKED;
        
        Node(Node parent, Event.Type type) { 
            this.parent = parent;
            this.type = type;
        }

        void invalidateBaked() {
            baked = EMPTY_BAKED;
        }

        Listeners baked() {
			return (needsBake() ? bake() : this.baked).clone();
		}
		
		Listeners bake() {
            Listener[] bake = listeners.toArray(Listener[]::new);
            if (parent == null)
                return this.baked = Listeners.of(bake);
            return this.baked = parent == null ? Listeners.of(bake) 
                : type == Event.Type.BLANK ? parent.baked().plusBlank(bake) : parent.baked().plusValued(bake);
		}

        boolean needsBake() {
            return baked == EMPTY_BAKED;
        }

        void add(Listener listener) {
            listeners.add(listener);
            invalidateBaked();
            if (children != null)
                for (Node child : children.values())
                    child.invalidateBaked();
        }

        Node get(Event child) {
            if (children == null)
                return null;
            return children.get(child);
        }

        Node getOrAdd(Event child) {
            if (children == null) {
                children = new IdentityHashMap<>(8);
                Node node = new Node(this, child.type());
                children.put(child, node);
                return node;
            }
            Node node = children.get(child);
            if (node == null) {
                node = new Node(this, child.type());
                children.put(child, node);
                return node;
            }
            return node;
        }

    }

    final Node root = new Node(null, Event.ROOT.type());

    public ListenerMap() { }

    public <T> Listeners<? super T> get(Event<T> event) {
        if (event == null)
            throw new NullPointerException();
            int length = event.generation();
            Event[] lineage = event.lineage();
            Node node = root;
            for (int i = length - 2; i >= 0; i--) {
                Node child = node.get(lineage[i]);
                if (child == null)
                    break;
                node = child;
            }
            return node.baked();
    }

    public <T> void add(Event<T> event, Listener<? super T> listener) {
        if (event == Event.ROOT)
            root.add(listener);
        else {
            Event[] lineage = event.lineage();
            Node node = root;
            for (int i = lineage.length - 2; i >= 0; i--)
                node = node.getOrAdd(lineage[i]);
            node.add(listener);
        }
    }

}
