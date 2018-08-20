package cz.metacentrum.perun.utils.graphs.generators;

import cz.metacentrum.perun.utils.graphs.GraphEdge;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <b>Class that defines relations in graph.</b>
 *
 * <p>
 * New relations can be added with method 'addEntitiesData' where the expected format
 * is a {@link Map} of {@link T} entities where every entity has a {@link Set} of target entities.
 * In other words, there is an oriented Edge between enetity and its target entities.
 * After specifying entities data, you have to set edge type with method 'withEdgeType'.
 *
 * Example: graphDefinition.addEntitiesData(data).withEdgeType(GraphEdge.Type.BOLD);
 * </p>
 *
 * <p>
 * New relations can also be added with method 'addEntity'. After specifying the entity,
 * you have to set target entities with method 'withTargetEntities'. After that you must set
 * the graph edge type.
 *
 * Example: graphDefinition.addEntity(entity).withTargetEntities(targetEntities).withEdgeType(GraphEdge.Type.BOLD);
 * </p>
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class GraphDefinition<T> {

	private Map<GraphEdge.Type, Map<T, Set<T>>> data = new HashMap<>();

	Set<GraphEdge.Type> getEdgeTypes() {
		return Collections.unmodifiableSet(data.keySet());
	}

	Map<T, Set<T>> getEdgeData(GraphEdge.Type edgeType) {
		return Collections.unmodifiableMap(data.get(edgeType));
	}

	/**
	 * Method used to add entities data to definition. After calling this method
	 * you have to set the Graph edge type.
	 *
	 * @param entityWithTargetEntities entities data
	 * @return object defining that entities data has been set
	 */
	public AddEntitiesData addEntitiesData(Map<T, Set<T>> entityWithTargetEntities) {
		return new AddEntitiesData(entityWithTargetEntities, this);
	}

	/**
	 * Method used to add entity to definition. After calling this method
	 * you have to set the target entities.
	 *
	 * @param entity entity
	 * @return object defining that entity data has been set
	 */
	public AddEntity addEntity(T entity) {
		return new AddEntity(entity, this);
	}

	/**
	 * Class representing the state of adding new data to the {@link GraphDefinition}.
	 * This state represents that entities data has been set and the graph edge type needs to be set.
	 */
	public class AddEntitiesData {
		private Map<T, Set<T>> entityWithTargetEntities;
		private GraphDefinition<T> graphDefinition;

		public AddEntitiesData(Map<T, Set<T>> entityWithTargetEntities, GraphDefinition<T> graphDefinition) {
			this.entityWithTargetEntities = entityWithTargetEntities;
			this.graphDefinition = graphDefinition;
		}

		/**
		 * Method used to set the edge type for data that has been passed before.
		 *
		 * @param edgeType edge type
		 * @return graph definition
		 */
		public GraphDefinition<T> withEdgeType(GraphEdge.Type edgeType) {
			if (!data.containsKey(edgeType)) {
				data.put(edgeType, new HashMap<>());
			}

			for (T node : entityWithTargetEntities.keySet()) {

				HashSet<T> targetNodes = new HashSet<>(entityWithTargetEntities.get(node));

				data.get(edgeType).put(node, targetNodes);
			}

			return graphDefinition;
		}
	}

	/**
	 * Class representing the state of adding new entity to the {@link GraphDefinition}.
	 * This state represents that entity has been set and target entities need to be set.
	 */
	public class AddEntity {
		private T entity;
		private GraphDefinition<T> graphDefinition;

		public AddEntity(T entity, GraphDefinition<T> graphDefinition) {
			this.entity = entity;
			this.graphDefinition = graphDefinition;
		}

		/**
		 * Method used to set the target entities to entity that has been set before.
		 *
		 * @param targetEntities target entitites
		 * @return object defining that target nodes has been set
		 */
		public WithTargetEntities withTargetEntities(Set<T> targetEntities) {
			return new WithTargetEntities(graphDefinition, entity, targetEntities);
		}
	}

	/**
	 * Class representing the state of adding target entities.
	 * This state represents that target entities has been set for entity specified before
	 * and the graph edge type needs to be set.
	 */
	public class WithTargetEntities {
		private GraphDefinition<T> graphDefinition;
		private T entity;
		private Set<T> targetEntities;

		public WithTargetEntities(GraphDefinition<T> graphDefinition, T entity, Set<T> targetEntities) {
			this.graphDefinition = graphDefinition;
			this.entity = entity;
			this.targetEntities = targetEntities;
		}

		/**
		 * Method used to set the edge type for data that has been passed before.
		 *
		 * @param edgeType edge type
		 * @return graph definition
		 */
		public GraphDefinition<T> withEdgeType(GraphEdge.Type edgeType) {
			if (!data.containsKey(edgeType)) {
				data.put(edgeType, new HashMap<>());
			}

			data.get(edgeType).put(entity, targetEntities);

			return graphDefinition;
		}
	}
}
