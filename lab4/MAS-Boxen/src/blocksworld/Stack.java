package blocksworld;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import blocksworld.Predicate.Type;

/**
 * Class representing a stack of blocks in blocksworld.
 * <p>
 * The state of the stack can be accessed as a list of predicates ({@link #getPredicates()}) or directly using the
 * properties of the stack, using {@link #getBlocks()}, {@link #contains(Block)}, {@link #isOn(Block, Block)} and
 * {@link #isClear(Block)}.
 * <p>
 * For methods working on a group of {@link Stack}s, see {@link BlocksWorld}.
 * 
 * 
 * @author andreiolaru
 */
public class Stack
{
	/**
	 * The blocks in the stack which are not locked. The first element is the top block.
	 */
	Deque<Block>	blocks			= new LinkedList<>();
	
	/**
	 * The blocks in the stack which are locked. The first element is the topmost block. These blocks cannot be moved
	 * and cannot be unlocked.
	 */
	List<Block>		lockedBlocks	= new LinkedList<>();
	
	/**
	 * Creates a new stack, based on the given block.
	 * 
	 * @param base
	 *            - the base of the stack.
	 */
	Stack(Block base)
	{
		blocks.push(base);
	}
	
	/**
	 * Creates a stack with the given blocks (used at world construction).
	 * 
	 * @param blocks
	 *            - the free blocks in the stack.
	 * @param lockedBlocks
	 *            - the locked blocks in the stack.
	 */
	Stack(List<Block> blocks, List<Block> lockedBlocks)
	{
		this.blocks.addAll(blocks);
		this.lockedBlocks.addAll(lockedBlocks);
	}
	
	/**
	 * Checks the sanity of this stack.
	 */
	protected void sane()
	{
		if(blocks.isEmpty() && lockedBlocks.isEmpty())
			throw new IllegalStateException("This stack contains no blocks");
	}
	
	/**
	 * Unstacks the topmost block of this stack.
	 * 
	 * @param toUnstack
	 *            - the block to unstack from the tower (or a {@link Block} instance with the same label).
	 * @param unstackFrom
	 *            - the block that is under it (or a {@link Block} instance with the same label).
	 * @return the block that was unstacked (the actual instance in this stack).
	 * 
	 * @throws IllegalArgumentException
	 *             if the block cannot be unstacked from this stack
	 * @throws IllegalStateException
	 *             if the stack is not sane.
	 */
	protected Block unstack(Block toUnstack, Block unstackFrom)
	{
		sane();
		if(blocks.isEmpty())
			throw new IllegalArgumentException("All blocks in this stack are locked.");
		if(blocks.peekFirst().equals(toUnstack))
		{
			if(isSingleBlock())
				throw new IllegalArgumentException("Block [" + toUnstack + "] is directly on the table. Use pickup.");
			Iterator<Block> it = getBlocks().iterator();
			it.next();
			if(!it.next().equals(unstackFrom))
				throw new IllegalArgumentException("Block [" + toUnstack + "] is is not over [" + unstackFrom + "].");
			return blocks.poll();
		}
		throw new IllegalArgumentException("Block [" + toUnstack + "] is not the topmost block of this stack.");
	}
	
	/**
	 * Stacks a block on top of this stack.
	 * 
	 * @param toStack
	 *            - block to stack.
	 * @param stackOver
	 *            - <code>true</code> if this block should be accepted even if not previously existing in this station.
	 * 
	 * @throws IllegalArgumentException
	 *             if the block to stack over is incorrect.
	 * @throws IllegalStateException
	 *             if the stack is not sane.
	 */
	protected void stack(Block toStack, Block stackOver)
	{
		sane();
		if(isClear(stackOver))
		{
			blocks.push(toStack);
			return;
		}
		throw new IllegalStateException("Block [" + stackOver + "] is not at the topmost block of this stack.");
	}
	
	/**
	 * @param block
	 *            - block to lock. Locked blocks can never be moved again.
	 */
	protected void lock(Block block)
	{
		sane();
		if(lockedBlocks.contains(block))
			throw new IllegalArgumentException("Block [" + block + "] is already locked.");
		if(!isOnTable(block) && !lockedBlocks.contains(getBelow(block)))
			throw new IllegalArgumentException("The block under [" + block + "] is not locked.");
		lockedBlocks.add(0, block);
		blocks.removeLast();
	}
	
	/**
	 * @return <code>true</code> if there is only one block in the stack.
	 */
	public boolean isSingleBlock()
	{
		return getBlocks().size() == 1;
	}
	
	/**
	 * @param block
	 *            - the block to check.
	 * @return <code>true</code> if the block is in this stack; <code>false</code> otherwise.
	 */
	public boolean contains(Block block)
	{
		return getBlocks().contains(block);
	}
	
	/**
	 * Checks if the given block is the topmost block of this stack.
	 * 
	 * @param block
	 *            - the block to check.
	 * @return <code>true</code> if it is indeed the topmost block; <code>false</code> in any other case (including if
	 *         the given block is not in the stack.
	 */
	public boolean isClear(Block block)
	{
		sane();
		return getTopBlock().equals(block);
	}
	
	/**
	 * Checks if the given block is the bottom block of this stack.
	 * 
	 * @param block
	 *            - the block to check.
	 * @return <code>true</code> if it is indeed the bottom block; <code>false</code> in any other case (including if
	 *         the given block is not in the stack.
	 */
	public boolean isOnTable(Block block)
	{
		sane();
		return getBottomBlock().equals(block);
	}
	
	/**
	 * Checks if the first block is currently immediately on top of the second block.
	 * 
	 * @param topBlock
	 *            - first block.
	 * @param bottomBlock
	 *            - second block.
	 * @return <code>true</code> if the condition is fulfilled.
	 * 
	 * @throws IllegalArgumentException
	 *             if the blocks are not both in this stack.
	 */
	public boolean isOn(Block topBlock, Block bottomBlock)
	{
		sane();
		if(!contains(topBlock))
			throw new IllegalArgumentException("Block [" + topBlock + "] is not in this stack.");
		if(!contains(bottomBlock))
			throw new IllegalArgumentException("Block [" + bottomBlock + "] is not in this stack");
		
		for(Iterator<Block> it = getBlocks().iterator(); it.hasNext();)
			if(it.next().equals(topBlock) && it.hasNext() && it.next().equals(bottomBlock))
				return true;
		return false;
	}
	
	/**
	 * @return the topmost block of the stack.
	 */
	public Block getTopBlock()
	{
		return getBlocks().get(0);
	}
	
	/**
	 * @return the bottom block of the stack.
	 */
	public Block getBottomBlock()
	{
		return getBlocks().get(getBlocks().size() - 1);
	}
	
	/**
	 * @param block
	 *            - the block to search for.
	 * @return the block immediately above it, if any; <code>null</code> otherwise.
	 * @throws IllegalArgumentException
	 *             if the block is not in this stack.
	 */
	public Block getAbove(Block block)
	{
		if(!contains(block))
			throw new IllegalArgumentException("Block [" + block + "] is not in this stack.");
		Block above = null;
		for(Block b : getBlocks())
		{
			if(b.equals(block))
				return above;
			above = b;
		}
		return null; // should never get here
	}
	
	/**
	 * @param block
	 *            - the block to search for.
	 * @return the block below it, if any.
	 * @throws IllegalArgumentException
	 *             if the block is not in this stack.
	 */
	public Block getBelow(Block block)
	{
		if(!contains(block))
			throw new IllegalArgumentException("Block [" + block + "] is not in this stack.");
		boolean found = false;
		for(Block b : getBlocks())
		{
			if(found)
				return b;
			if(b.equals(block))
				found = true;
		}
		return null;
	}
	
	/**
	 * @return all the blocks in the stack. The first element is the top block.
	 */
	public List<Block> getBlocks()
	{
		List<Block> ret = new LinkedList<>(blocks);
		ret.addAll(lockedBlocks);
		return ret;
	}
	
	/**
	 * @param block
	 *            - the block to check.
	 * @return <code>true</code> if the block is locked.
	 */
	public boolean isLocked(Block block)
	{
		return lockedBlocks.contains(block);
	}
	
	/**
	 * @return the predicates that represent the stack.
	 */
	public List<Predicate> getPredicates()
	{
		Block above = null;
		List<Predicate> ret = new LinkedList<>();
		for(Block b : getBlocks())
		{
			if(above == null)
				ret.add(new Predicate(Type.CLEAR, b));
			else
				ret.add(new Predicate(Type.ON, above, b));
			above = b;
		}
		ret.add(new Predicate(Type.ONTABLE, above));
		return ret;
	}
	
	@Override
	public String toString()
	{
		return getBlocks().toString();
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Stack && ((Stack) obj).getBlocks().equals(getBlocks());
	}
	
	@Override
	public int hashCode()
	{
		return getBlocks().hashCode();
	}
}
