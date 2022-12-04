package blocksworld;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/**
 * Class representing the (a) world state in blocks world.
 * <p>
 * 
 * @author Andrei Olaru
 */
public class BlocksWorld
{
	/**
	 * Character for representing empty space in the input file.
	 */
	public static final char	EMPTY_SPACE	= '.';
	
	/**
	 * The stacks of blocks in this world.
	 */
	protected List<Stack>		stacks;
	
	/**
	 * List of all blocks initially in this world.
	 */
	Set<Block>					allBlocks	= new HashSet<>();
	
	/**
	 * Constructor used by {@link #clone()}, or when constructing an instance from scratch.
	 */
	public BlocksWorld()
	{
		// nothing to do
	}
	
	/**
	 * Adds a stack to this world (used when constructing an instance from scratch).
	 * 
	 * @param stack - the stack to add.
	 */
	public void addStack(Stack stack)
	{
		stacks.add(stack);
		allBlocks.addAll(stack.getBlocks());
	}
	
	/**
	 * Reads the world state from the input and creates a list of {@link Stack} instances.
	 * 
	 * @param input
	 *            - the input.
	 * @throws IOException
	 *             if the input is corrupted.
	 */
	public BlocksWorld(InputStream input) throws IOException
	{
		stacks = new LinkedList<>();
		try (Scanner scan = new Scanner(input))
		{
			int iLevel = 0;
			
			Map<Integer, Map<Integer, Block>> ts = new HashMap<>();
			while(scan.hasNextLine())
			{
				String line = scan.nextLine().trim();
				if(line.length() == 0)
				{
					if(iLevel > 0)
						break;
					continue;
				}
				
				Integer xc = Integer.valueOf(0);
				while(line.length() > 0)
				{
					Block b = new Block(line.charAt(0));
					line = line.substring(1);
					
					if(b.getLabel() != EMPTY_SPACE)
					{
						if(allBlocks.contains(b))
							throw new IOException("duplicate blocks not allowed.");
						if(ts.containsKey(xc))
						{
							if(!ts.get(xc).containsKey(Integer.valueOf(iLevel - 1)))
								throw new IOException("space found in tower.");
							ts.get(xc).put(Integer.valueOf(iLevel), b);
						}
						else
						{
							Map<Integer, Block> t = new HashMap<>();
							t.put(Integer.valueOf(iLevel), b);
							ts.put(xc, t);
						}
					}
					xc = Integer.valueOf(xc.intValue() + 1);
				}
				iLevel++;
			}
			for(Map<Integer, Block> t : ts.values())
				if(!t.containsKey(Integer.valueOf(iLevel - 1)))
					throw new IOException("tower doesn't reach the table.");
			for(Entry<Integer, Map<Integer, Block>> entry : ts.entrySet())
			{
				List<Block> tower = new LinkedList<>();
				for(int i = 0; i < iLevel; i++)
					if(entry.getValue().containsKey(Integer.valueOf(i)))
					{
						Block b = entry.getValue().get(Integer.valueOf(i));
						tower.add(b);
						if(!allBlocks.add(b))
							throw new IllegalStateException("duplicate block found");
					}
				stacks.add(new Stack(tower, new LinkedList<>()));
			}
		}
	}
	
	/**
	 * @return all blocks known to this world.
	 */
	public Set<Block> allBlocks()
	{
		return new HashSet<>(allBlocks);
	}
	
	/**
	 * Checks if the specified block has ever existed in this world.
	 * 
	 * @param block
	 *            - the block to search for.
	 * @return <code>true</code> if the block is known (existed in the initial state).
	 */
	public boolean exists(Block block)
	{
		return allBlocks.contains(block);
	}
	
	/**
	 * Checks if the specified block is currently in a stack in this world.
	 * 
	 * @param block
	 *            - the block to search for.
	 * @return <code>true</code> if the block is in any stack.
	 */
	public boolean contains(Block block)
	{
		for(Stack s : stacks)
			if(s.contains(block))
				return true;
		return false;
	}
	
	/**
	 * Gets the {@link Stack} containing a given {@link Block}.
	 * 
	 * @param block
	 *            - the block to search for.
	 * @return the {@link Stack} containing the block.
	 * @throws IllegalArgumentException
	 *             if the block does not exist in the world.
	 */
	public Stack getStack(Block block)
	{
		if(!allBlocks.contains(block))
			throw new IllegalArgumentException("Block [" + block + "] has never existed in this world.");
		for(Stack s : stacks)
			if(s.getBlocks().contains(block))
				return s;
		throw new IllegalArgumentException("Block [" + block + "] is not currently in any stack");
	}
	
	/**
	 * Picks up a block from the table. Removes the stack containing it but does not remove it from the list of all
	 * blocks.
	 * 
	 * @param block
	 *            - the block to pick up (or a {@link Block} instance with the same label).
	 * @return the block that was just picked up (the actual instance in the world).
	 * 
	 * @throws IllegalArgumentException
	 *             if the block does not exist in the world.
	 */
	public Block pickUp(Block block)
	{
		Stack s = getStack(block);
		if(!s.isSingleBlock())
			throw new IllegalArgumentException("Block [" + block + "] is not in a single-block stack.");
		if(s.blocks.isEmpty())
			throw new IllegalArgumentException("Block [" + block + "] is locked.");
		stacks.remove(s);
		return s.getTopBlock();
	}
	
	/**
	 * Creates a new {@link Stack}, based on the given {@link Block}, and adds it to this world.
	 * 
	 * @param block
	 *            - the block to put down.
	 * @param currentStack
	 *            - position before which to insert the new stack.
	 * @return the newly created stack.
	 */
	public Stack putDown(Block block, Stack currentStack)
	{
		if(!allBlocks.contains(block))
			throw new IllegalArgumentException("Block [" + block + "] has never existed in this world.");
		Stack s = new Stack(block);
		stacks.add(stacks.indexOf(currentStack), s);
		return s;
	}
	
	/**
	 * Unstacks one block from another.
	 * 
	 * @param toUnstack
	 *            - the block to unstack from the tower (or a {@link Block} instance with the same label).
	 * @param unstackFrom
	 *            - the block that is under it (or a {@link Block} instance with the same label).
	 * @return the block that was unstacked (the actual instance in the world).
	 * 
	 * @throws IllegalArgumentException
	 *             if the block cannot be unstacked from this stack
	 */
	public Block unstack(Block toUnstack, Block unstackFrom)
	{
		return getStack(toUnstack).unstack(toUnstack, unstackFrom);
	}
	
	/**
	 * Stacks a block on top of another.
	 * 
	 * @param toStack
	 *            - block to stack.
	 * @param stackOver
	 *            - block to stack it over.
	 * 
	 * @throws IllegalArgumentException
	 *             if the block to stack over is incorrect.
	 */
	public void stack(Block toStack, Block stackOver)
	{
		getStack(stackOver).stack(toStack, stackOver);
	}
	
	/**
	 * Locks a block. This can only be on table or on another block.
	 * <p>
	 * A locked block cannot be moved anymore.
	 * 
	 * @param block
	 *            - the block to lock.
	 * @throws IllegalArgumentException
	 *             if the block does not exist in the world.
	 */
	public void lock(Block block)
	{
		getStack(block).lock(block);
	}
	
	/**
	 * Checks if a block is currently on the table.
	 * 
	 * @param block
	 *            - the block to search for.
	 * @return <code>true</code> if the block is on the table; <code>false</code> if the block is over another block.
	 * 
	 * @throws IllegalArgumentException
	 *             if the block cannot be found.
	 */
	public boolean isOnTable(Block block)
	{
		return getStack(block).isOnTable(block);
	}
	
	/**
	 * Obtains a copy of the current state, using the same blocks instances.
	 */
	@Override
	public BlocksWorld clone()
	{
		BlocksWorld ret = new BlocksWorld();
		ret.allBlocks = new HashSet<>(allBlocks);
		ret.stacks = new LinkedList<>();
		for(Stack s : stacks)
			ret.stacks.add(new Stack(new LinkedList<>(s.blocks), s.lockedBlocks));
		return ret;
	}
	
	@Override
	public String toString()
	{
		return toString(0, null, null, true);
	}
	
	/**
	 * @param stackSpace
	 *            - the width allocated to a stack, in number of characters.
	 * @param prefixes
	 *            - stuff to print before the main display, for each stack; each element of the list is printed on a
	 *            different line.
	 * @param suffixes
	 *            - stuff to print after the main display, for each stack; each element of the list is printed on a
	 *            different line.
	 * @param printTable
	 *            - if <code>true</code>, the "table" on which the blocks sit will also be printed.
	 * @return the {@link String} representation.
	 */
	public String toString(int stackSpace, Map<Stack, List<String>> prefixes, Map<Stack, List<String>> suffixes,
			boolean printTable)
	{
		int stack_space = Math.max(stackSpace, 3);
		int maxHeight = 0;
		for(Stack stack : stacks)
			if(maxHeight <= stack.getBlocks().size())
				maxHeight = stack.getBlocks().size();
			
		String ret = "";
		
		ret += printAdditional(prefixes, stack_space);
		
		for(int y = maxHeight; y > 0; y--)
		{
			ret += " ";
			for(Stack stack : stacks)
			{
				List<Block> blocks = stack.getBlocks();
				if(blocks.size() >= y)
				{
					Block block = (Block) blocks.toArray()[blocks.size() - y];
					boolean lck = stack.lockedBlocks.contains(block);
					ret += (lck ? "{" : "[") + block + (lck ? "}" : "]");
				}
				else
					ret += "   ";
				for(int x = 3; x < stack_space; x++)
					ret += " ";
			}
			// for(int i = x; i <= (maxX + 1) * 3; i++)
			// ret += " ";
			ret += "\n";
		}
		
		if(printTable)
			for(int i = -1; i <= stacks.size() * stack_space + 1; i++)
				ret += "=";
		ret += printAdditional(suffixes, stack_space);
		
		return ret;
	}
	
	/**
	 * @param add
	 *            - additional stuff to print (see {@link #toString(int, Map, Map, boolean)}).
	 * @param stackSpace
	 *            - see {@link #toString(int, Map, Map, boolean)}.
	 * @return string to add.
	 */
	protected String printAdditional(Map<Stack, List<String>> add, int stackSpace)
	{
		int maxA = 0;
		String ret = "";
		if(add == null)
			return "";
		for(List<String> a : add.values())
			if(a.size() > maxA)
				maxA = a.size();
		for(int y = 0; y < maxA; y++)
		{
			for(Stack stack : stacks)
				if(add.containsKey(stack))
				{
					ret += add.get(stack).get(y);
					for(int i = add.get(stack).get(y).length(); i < stackSpace; i++)
						ret += " ";
				}
				else
					for(int i = 0; i < stackSpace; i++)
						ret += " ";
			ret += "\n";
		}
		return ret + "\n";
	}
	
	/**
	 * @return the state of the world as a set of {@link Predicate} instances.
	 */
	public Collection<Predicate> getPredicates()
	{
		List<Predicate> ret = new LinkedList<>();
		for(Stack s : stacks)
			ret.addAll(s.getPredicates());
		return ret;
	}
	
	/**
	 * @return the stacks in this world.
	 */
	public List<Stack> getStacks()
	{
		return new LinkedList<>(stacks);
	}
}
