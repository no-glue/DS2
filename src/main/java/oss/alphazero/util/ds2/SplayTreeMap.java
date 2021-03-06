package oss.alphazero.util.ds2;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Implements a top-down Splay Tree based on original work
 * of Danny Sleator available at http://www.link.cs.cmu.edu/splay/
 * with partial support for {@link Map} interface.
 * <ol>
 * <li>Modified for Java 5 and later, using Java generics.</li>
 * <li>Modified API for clarity</li>
 * <li>Modified to (partially) support Map<K, V> semantics - original
 * coupled node key with node value</li>
 * <li>Null key is clearly not allowed.</li>
 * <li>Null values are allowed.</li>
 * </ol>
 * 
 * @param K SplayTreeMap node key type
 * @param V SplayTreeMap node value type
 * @author Danny Sleator <sleator@cs.cmu.edu>
 * @author Joubin Houshyar <alphazero@sensesay.net>
 * 
 * This code is in the public domain.
 * 
 * @update:  Feb 10, 2012
 * 
 */
public class SplayTreeMap<K extends Comparable<K>, V> implements Map<K,V>
{
	// ------------------------------------------------------------------------
	// Inner class: BinaryNode
	// ------------------------------------------------------------------------
	private class Node implements Map.Entry<K, V>
	{
		Node(K key, V value) {
			this.key = key;
			this.value = value;
			left = right = null;
		}

		/** node key */
		K key;
		/** node value */
		V value;
		/** left child */
		Node left;
		/** right child */
		Node right;

		/* (non-Javadoc) @see java.util.Map.Entry#getKey() */
		@Override final
		public K getKey() {
			return key;
		}
		/* (non-Javadoc) @see java.util.Map.Entry#getValue() */
		@Override final
		public V getValue() {
			return value;
		}
		/* (non-Javadoc) @see java.util.Map.Entry#setValue(java.lang.Object) */
		@Override final
		public V setValue(V value) {
			V oldv = value;
			this.value = value;
			return oldv;
		} 
	}

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/** header node (changed from static - jh) */
	private final Node header = new Node(null, null); // For splay

	/** root node (initially null) */
	private Node root  = null;

	/** number of key-value mappings */
	private int size = 0;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public SplayTreeMap() { }

	// ------------------------------------------------------------------------
	// Inner Ops
	// ------------------------------------------------------------------------
	/** 
	 * This method just illustrates the top-down method of
	 * implementing the move-to-root operation and <b>is not used
	 * in this version</b>. 
	 */
	@SuppressWarnings("unused")
	private void moveToRoot(K key) {
		Node l, r, t;
		l = r = header;
		t = root;
		header.left = header.right = null;
		for (;;) {
			if (key.compareTo(t.key) < 0) {
				if (t.left == null) break;
				r.left = t;                                 /* link right */
				r = t;
				t = t.left;
			} else if (key.compareTo(t.key) > 0) {
				if (t.right == null) break;
				l.right = t;                                /* link left */
				l = t;
				t = t.right;
			} else {
				break;
			}
		}
		l.right = t.left;                                   /* assemble */
		r.left = t.right;
		t.left = header.right;
		t.right = header.left;
		root = t;
	}

	/**
	 * Internal method to perform a top-down splay.
	 * 
	 *   splay(key) does the splay operation on the given key.
	 *   If key is in the tree, then the BinaryNode containing
	 *   that key becomes the root.  If key is not in the tree,
	 *   then after the splay, root.key is either the greatest key
	 *   in the tree, or the lest key key in the tree.
	 *
	 *   This means, among other things, that if you splay with
	 *   a key that's larger than any in the tree, the rightmost
	 *   node of the tree becomes the root.  This property is used
	 *   in the delete() method.
	 */

	private void splay(K key) {
		Node l, r, t, y;
		l = r = header;
		t = root;
		header.left = header.right = null;
		for (;;) {
			if (key.compareTo(t.key) < 0) {
				if (t.left == null) break;
				if (key.compareTo(t.left.key) < 0) {
					y = t.left;                            /* rotate right */
					t.left = y.right;
					y.right = t;
					t = y;
					if (t.left == null) break;
				}
				r.left = t;                                 /* link right */
				r = t;
				t = t.left;
			} else if (key.compareTo(t.key) > 0) {
				if (t.right == null) break;
				if (key.compareTo(t.right.key) > 0) {
					y = t.right;                            /* rotate left */
					t.right = y.left;
					y.left = t;
					t = y;
					if (t.right == null) break;
				}
				l.right = t;                                /* link left */
				l = t;
				t = t.right;
			} else {
				break;
			}
		}
		l.right = t.left;                                   /* assemble */
		r.left = t.right;
		t.left = header.right;
		t.right = header.left;
		root = t;
	}

	// ------------------------------------------------------------------------
	// Public API : SplayTreeMap
	// ------------------------------------------------------------------------
	/**
	 * Insert into the key-value mapping into the tree. Size is incremented.
	 * @param key the item to insert.
	 * @return true if successfully added; false if item is already present.
	 * @throws IllegalArgumentException if key is null
	 */
	final public boolean insert(K key, V value) throws IllegalArgumentException {
		if(key == null)
			throw new IllegalArgumentException("null key");

		// if empty then just add it
		if (isEmpty()) {
			root = new Node(key, value);
			size++;
			return true;
		}

		splay(key);

		// check if key is already present
		int c;
		if ((c = key.compareTo(root.key)) == 0) 
			return false;


		// insert new node
		Node n = new Node(key, value);
		if (c < 0) {
			n.left = root.left;
			n.right = root;
			root.left = null;
		} else {
			n.right = root.right;
			n.left = root;
			root.right = null;
		}
		root = n;
		size++;

		return true;
	}

	/**
	 * Remove node from the tree.  Note that a splay operation
	 * is performed on tree even if the key does not exist.  
	 * 
	 * @param key of the node to remove.
	 * @return true if key was found and removed. false otherwise.
	 * @throws IllegalArgumentException if key is null
	 */
	final public boolean delete(K key) {
		if(key == null)
			throw new IllegalArgumentException("null key");

		// splay the tree - if key exists the root will be key
		splay(key);
		if (key.compareTo(root.key) != 0) {
			return false; // not found
		}

		// key exists and is root - delete it
		if (root.left == null) {
			root = root.right;
		} else {
			final Node x = root.right;
			root = root.left;
			splay(key);
			root.right = x;
		}
		size--;

		return true;
	}

	/**
	 * @return the smallest item in tree; null if empty
	 */
	final public K minKey() {
		Node x = root;
		if(root == null) 
			return null;
		while(x.left != null) 
			x = x.left;

		splay(x.key);

		return x.key;
	}

	/**
	 * @return the largest key in the tree; null if empty
	 */
	final public K maxKey() {
		if(isEmpty()) 
			return null;

		Node x = root;
		while(x.right != null) 
			x = x.right;

		splay(x.key);

		return x.key;
	}

	/**
	 * Find a node in the tree. Splay operation is applied
	 * to tree regardless of whether key specified exists or not.
	 * @return the node (now root) if contained; null otherwise
	 * @throws IllegalArgumentException if key is null
	 * 
	 */
	final public Node find(K key) {
		if(key == null)
			throw new IllegalArgumentException("null key");

		if (isEmpty()) 
			return null;

		splay(key);

		if(root.key.compareTo(key) != 0) 
			return null;

		return root;
	}

	/**
	 * Test if the tree is logically empty.
	 * @return true if empty, false otherwise.
	 */
	final public boolean isEmpty() {
		return root == null;
	}

	// ------------------------------------------------------------------------
	// Public API : Map<K, V>
	// ------------------------------------------------------------------------

	/* (non-Javadoc) @see java.util.Map#containsKey(java.lang.Object) */
	@SuppressWarnings("unchecked")
	@Override final
	public boolean containsKey(Object key) {
		boolean res = false;

		if(find((K)key) != null)
			res = true;

		return res;
	}

	/* (non-Javadoc) @see java.util.Map#get(java.lang.Object) */
	@SuppressWarnings("unchecked")
	@Override final
	public V get(Object key) {
		final Node node = find((K)key);
		if(node == null)
			return null;

		return node.value;
	}

	/* (non-Javadoc) @see java.util.Map#put(java.lang.Object, java.lang.Object) */
	@Override final
	public V put(K key, V value) {
		final Node node = find((K)key);
		if(node == null) {
			if(!insert(key, value))
				throw new RuntimeException("BUG: find returned null but insert failed!");
			return null; // successful insert of new key per Map#put
		}
		return node.setValue(value);
	}

	/* (non-Javadoc) @see java.util.Map#remove(java.lang.Object) */
	@SuppressWarnings("unchecked")
	@Override final
	public V remove(Object key) {
		final Node node = find((K)key);
		if(node == null)
			return null; // wasn't there; null per Map#remove

		// delete the node - save value for return
		V value = node.value;
		if(!delete((K)key))
			throw new RuntimeException("BUG: find returned node but delete failed!");

		return value;
	}

	/* (non-Javadoc) @see java.util.Map#size() */
	@Override final
	public int size() {
		return size;
	}

	/* (non-Javadoc) @see java.util.Map#putAll(java.util.Map) */
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for(K k : m.keySet())
			insert(k, m.get(k));
	}

	/** NOT SUPPORTED */
	@Override
	public void clear() {
		throw new RuntimeException ("Map<K,V>#clear is not supported!");
	}

	/** NOT SUPPORTED */
	@Override
	public boolean containsValue(Object value) {
		throw new RuntimeException ("Map<K,V>#containsValue is not supported!");
	}

	/** NOT SUPPORTED */
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		throw new RuntimeException ("Map<K,V>#entrySet is not supported!");
	}

	/** NOT SUPPORTED */
	@Override
	public Set<K> keySet() {
		throw new RuntimeException ("Map<K,V>#keySet is not supported!");
	}

	/** NOT SUPPORTED */
	@Override
	public Collection<V> values() {
		throw new RuntimeException ("Map<K,V>#values is not supported!");
	}
}