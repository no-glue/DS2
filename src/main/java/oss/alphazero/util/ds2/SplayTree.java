package oss.alphazero.util.ds2;

/**
 * Implements a top-down Splay Tree based on original work
 * of Danny Sleator available at http://www.link.cs.cmu.edu/splay/
 * <ol>
 * <li>Modified for Java 5 and later, using Java generics.</li>
 * <li>Modified API for clarity</li>
 * <li>Null key values are not allowed and will throw {@link IllegalArgumentException}</li>
 * </ol>
 * 
 * @param K SprayTree node key type
 * 
 * @author Danny Sleator <sleator@cs.cmu.edu>
 * @author Joubin Houshyar <alphazero@sensesay.net>
 * 
 * This code is in the public domain.
 * 
 * @update:  Feb 10, 2012
 * 
 */
public class SplayTree<K extends Comparable<K>>
{
	public class Node
	{
		Node(K key) {
			this.key = key;
			left = right = null;
		}
		/** node key */
		private K key;
		/** left child */
		private Node left;
		/** right child */
		private Node right; 
		
		/* read only accessors for traversals */
		final public K key() { return key; }
		final public Node right() { return right;}
		final public Node left() { return left;}
		@Override final
		public String toString () {
			K kR = right != null ? right.key : null;
			K kL = left != null ? left.key : null;
			return String.format("Node[%s] => (L:%s, R:%s)", key, kL, kR);
		}
	}

	// ------------------------------------------------------------------------
	// Properties
	// ------------------------------------------------------------------------

	/** root node (initially null) */
	private Node root = null;

	/** header node (changed from static - jh) */
	private final Node header = new Node(null); // For splay

	/** number of key-value mappings */
	private int size = 0;

	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------
	public SplayTree() { }

	// ------------------------------------------------------------------------
	// Inner Ops
	// ------------------------------------------------------------------------
	/** 
	 * This method just illustrates the top-down method of
	 * implementing the move-to-root operation and is not used
	 * in this version. 
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
	 *   then after the splay, key.root is either the greatest key
	 *   < key in the tree, or the lest key > key in the tree.
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
	// Public API : SplayTree
	// ------------------------------------------------------------------------
	/**
	 * Insert into the tree.
	 * @param key the item to insert.
	 * @return true if successfully added; false if item is already present.
	 */
	final public boolean insert(K key) {
		if(key == null)
			throw new IllegalArgumentException("null key");

		// if empty then just add it
		if (isEmpty()) {
			root = new Node(key);
			size++;
			return true;
		}

		splay(key);

		int c;
		if ((c = key.compareTo(root.key)) == 0) 
			return false;

		Node n = new Node(key);
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
	 * Remove item from the tree.  Note that a splay operation
	 * is performed on tree even if the key does not exist. 
	 * REVU(jh): renamed to delete 
	 * 
	 * @param key the item to remove.
	 * @return true if key was found and removed. false otherwise.
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
	 * Find a key in the tree. Splay operation is applied
	 * to tree regardless of whether item exists or not.
	 * This method, unlike all other public methods that take
	 * key as argument will not throw an IllegalArgumentException
	 * on null keys.  It simply returns false.
	 * @return true if contained; false otherwise
	 * 
	 * REVU (jh): this method should just return boolean.
	 * REVU (jh): renamed to contains
	 */
	final public boolean contains(K key) {
		if(key == null)
			return false;

		boolean res = false;

		if(find((K)key) != null)
			res = true;

		return res;
	}

	/**
	 * Splays the tree to find the node with given key.
	 * Can be used to begin traversals from a given key.
	 * (jh) Changed return type  as original simply returned the key again.
	 * @param key
	 * @return
	 */
	final public Node find(K key){
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

	final public int size() {
		return this.size;
	}



	// ========================================================================
	// Statics for adhoc tests -- remove at will
	// ========================================================================
	/**
	 * "test code stolen from Weiss" 
	 * Original tests of DS. <b>Enable assert!</b> 
	 * cleaned up to use type safe forms (jh)
	 */
	public static void main(String [ ] args)
	{
		SplayTree<Integer> t = new SplayTree<Integer>();
		final int NUMS = 40000;
		final int GAP  =   307;

		System.out.format("Running 'Weiss' ad-hoc tests with NUMS:%s GAP:%s\n", NUMS, GAP);
		System.out.format("*** NOTE: enable assert with Java -ea ...*** \n\n");

		// test inserts
		for(int i = GAP; i != 0; i = (i + GAP) % NUMS){
			boolean r = t.insert(i);
			assert r : "on insert " + i;
		}
		System.out.println(" - Inserts successfully completed");

		// test removes
		for(int i = 1; i < NUMS; i+= 2) {
			boolean r = t.delete(i);
			assert r : "on remove of " + i;
		}
		System.out.println(" - Removes successfully completed");

		// test min and max keys
		Integer maxkey = t.maxKey();
		assert maxkey != null : "max is null";

		Integer minkey = t.minKey();
		assert minkey != null : "min is null";

		if((minkey).intValue() != 2 || (maxkey).intValue() != NUMS - 2)
			System.err.println("FindMin or FindMax error!");

		System.out.println(" - Min/Max key tests successfully completed");

		// test for keys that should be contained
		for(int i = 2; i < NUMS; i+=2)
			if(!t.contains(i))
				System.err.println("Error: find fails for " + i);
		System.out.println(" - Positive containment tests successfully completed");

		// test for keys that should not be contained
		for(int i = 1; i < NUMS; i+=2)
			if(t.contains(i)) 
				System.err.println("Error: Found deleted item " + i);
		System.out.println(" - negative containment tests successfully completed");
	}
}