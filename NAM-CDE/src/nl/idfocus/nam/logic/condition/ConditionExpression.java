package nl.idfocus.nam.logic.condition;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.idfocus.nam.logic.action.Action;
import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;

public class ConditionExpression implements Condition 
{
	private static final Logger logger = LogFormatter.getConsoleLogger( ConditionExpression.class.getName() );
	private final Level loglevel = Level.FINE;

	private Action left;
	private Action right;
	private Equality equality;

	public ConditionExpression() 
	{
		this.left = null;
		this.right = null;
		this.equality = null;
	}

	@Override
	public boolean evaluate( DataResponse attrs ) 
	{
		logger.log( loglevel, " Condition: starting evaluate() on attributes" );
		if ( this.equality == null || this.left == null || this.right == null )
		{
			logger.log( loglevel, " Condition: one or more components are null!" );
			return false;
		}
		String[] lefts = this.left.perform(attrs);
		logger.log( loglevel, " Condition: perform() left returned: "+Arrays.toString(lefts)+"." );
		String[] rights = this.right.perform(attrs);
		logger.log( loglevel, " Condition: perform() right returned: "+Arrays.toString(rights)+"." );
		switch ( this.equality ) 
		{
			case EQUALS:
				return equals( lefts, rights, false );
			case EQUALS1:
				return equals( lefts, rights, true );
			case GTEQUALS:
				return greater( lefts, rights, true );
			case GTEQUALS1:
				return greater( lefts, rights, true );
			case LTEQUALS:
				return less( lefts, rights, true );
			case LTEQUALS1:
				return less( lefts, rights, true );
			case GREATER:
				return greater( lefts, rights, false );
			case LESS:
				return less( lefts, rights, false );
			case CONTAINS:
				return contains( lefts, rights, false );
			case CONTAINS1:
				return contains( lefts, rights, true );
			case CONTAINS2:
				return contains( lefts, rights, true );
			case NONE:
				return false;
		}
		return false;
	}

//	public void addLeft( Action value )
//	{
//		if ( this.left == null )
//			this.left = value;
//	}
//
//	public void addRight( Action value ) 
//	{
//		if ( this.right == null )
//			this.right = value;
//	}

	public void addAction( Action value ) throws ConditionException
	{
		if ( this.left == null )
			this.left = value;
		else if ( this.right == null )
			this.right = value;
		else
			throw new ConditionException( "Expression: more than two actions found" );
	}

	public void addEquality( String value )
	{
		if ( this.equality == null )
		{
			for ( Equality eq : Equality.values() )
			{
				if ( eq.value.equals( value ) )
				{
					this.equality = eq;
					break;
				}
			}
			// TODO throw error here
			if ( this.equality == null )
				this.equality = Equality.NONE;
		}
	}

	@Override
	public void addCondition(Condition condition) throws ConditionException
	{
		throw new ConditionException( "Expression: cannot nest other conditions" );
	}

	private enum Equality
	{
		EQUALS    ( "=",   1),
		EQUALS1   ( "==",  2),
		GTEQUALS  ( ">=",  3),
		GTEQUALS1 ( "=>",  3),
		LTEQUALS  ( "<=",  5),
		LTEQUALS1 ( "=<",  5),
		CONTAINS  ( "~",   7),
		CONTAINS1 ( "=~",  8),
		CONTAINS2 ( "~=",  9),
		GREATER   ( ">",  10),
		LESS      ( "<",  11),
		NONE      ( "",   99),
		;
		
		private final String value;

		private Equality( String value, int type ) 
		{
			this.value = value;
		}
		
	}

	private boolean equals( String[] lefts, String[] rights, boolean honorcase )
	{
		for ( String left : lefts )
		{
			for ( String right : rights )
			{
				if ( honorcase && left.equals(right) )
					return true;
				else if ( !honorcase && left.equalsIgnoreCase(right) )
					return true;
			}
		}
		return false;
	}

	private boolean greater( String[] lefts, String[] rights, boolean equals )
	{
		for ( String left : lefts )
		{
			for ( String right : rights )
			{
				try
				{
					int iLeft  = Integer.parseInt(left);
					int iRight = Integer.parseInt(right);
					if ( equals && iLeft >= iRight )
						return true;
					else if ( !equals && iLeft > iRight )
						return true;
				}
				catch (Exception e)
				{
					if ( equals && left.startsWith(right) )
						return true;
					else if ( !equals && left.toLowerCase().startsWith(right.toLowerCase()) )
						return true;
				}
			}
		}		
		return false;
	}

	private boolean less( String[] lefts, String[] rights, boolean equals )
	{
		return greater( rights, lefts, equals );
	}

	private boolean contains( String[] lefts, String[] rights, boolean honorcase )
	{
		for ( String left : lefts )
		{
			for ( String right : rights )
			{
				if ( honorcase && left.contains(right) )
					return true;
				else if ( !honorcase && left.toLowerCase().contains(right.toLowerCase() ) )
					return true;
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder msg = new StringBuilder();
		msg.append( "'Expression' condition contains " );
		if (! (this.left == null))
			msg.append( "left action: " )
			   .append( this.left.toString() );
		if (! (this.right == null))
			msg.append( "right action: " )
			   .append( this.right.toString() );
		if (! (this.equality == null))
			msg.append( this.equality.name() )
			   .append( " equality" );
		return msg.toString();
	}

}
