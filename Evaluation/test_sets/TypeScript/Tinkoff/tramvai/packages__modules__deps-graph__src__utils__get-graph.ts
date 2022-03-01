import { commandLineListTokens } from '@tramvai/core';
import type { Container, RecordProvide, DI_TOKEN } from '@tinkoff/dippy';

interface Deps {
  di: typeof DI_TOKEN;
  searchValue: string;
}

export interface RecordsGraph {
  token: string;
  moduleName?: string;
  record: RecordProvide<any>;
  deps: RecordsGraph[];
  rank: number;
  multi: boolean;
  multiInstance: boolean;
  match: boolean;
  parentEdgeMatch?: boolean;
  childEdgeMatch?: boolean;
  parentNode: RecordsGraph | null;
}

function traverseGraphUp(node: RecordsGraph, cb) {
  if (node) {
    cb(node);
    traverseGraphUp(node.parentNode, cb);
  }
}

export const getGraph = ({ di, searchValue }: Deps) => {
  function getDepsFromRecord(
    container: Container,
    parentNode: RecordsGraph,
    rank: number,
    record: RecordProvide<any>
  ) {
    const deps: RecordsGraph[] = [];
    Object.values(record.resolvedDeps).forEach((k) => {
      const depGraph = buildDepsGraph(
        container,
        parentNode,
        rank,
        // @ts-ignore
        k.token ? k.token.toString() : k.toString()
      );
      if (depGraph) {
        deps.push(depGraph);
      }
    });

    return deps;
  }

  function buildDepsGraph(
    container: Container,
    parentNode: RecordsGraph | null,
    rank: number,
    token: string,
    recordOfMulti?: RecordProvide<any>
  ): RecordsGraph | null {
    const record = recordOfMulti || container.getRecord(token);

    if (!record) {
      return null;
    }

    const moduleName = record.stack
      ? /\/(module(?:-|s\/)[\w-]*?)\//.exec(record.stack)?.[1].replace('modules/', '')
      : undefined;

    const searchMatch =
      searchValue &&
      ((token && token.indexOf(searchValue) !== -1) ||
        (moduleName && moduleName.indexOf(searchValue) !== -1));
    if (searchMatch) {
      traverseGraphUp(parentNode, (g) => {
        // eslint-disable-next-line no-param-reassign
        g.parentEdgeMatch = true;
      });
    }

    const node: RecordsGraph = {
      token,
      moduleName,
      record,
      match: searchMatch,
      childEdgeMatch: parentNode && (parentNode.match || parentNode.childEdgeMatch),
      parentNode,
      deps: [],
      rank,
      multi: !!record.multi,
      multiInstance: !!record.multi || !!recordOfMulti,
    };
    if (record.multi?.length) {
      node.deps = record.multi.reduce(
        (acc, r) => acc.concat(buildDepsGraph(container, node, rank + 1, token, r)),
        []
      );
    } else if (record?.resolvedDeps) {
      node.deps = getDepsFromRecord(container, node, rank + 1, record);
    }
    return node;
  }

  function formatGraphToDot(rootGraph: RecordsGraph): string {
    const edges: Array<{ from: string; to: string }> = [];
    const nodes: Array<{ id: string; label: string; multiInstance: boolean; match: boolean }> = [];
    const groupsRank: Record<string, number> = {};
    const groupsByRank: Record<string, string[]> = {};

    function recurse(parentNodeId: string | null, node: RecordsGraph) {
      const nodeId = node.moduleName ? `${node.token}:${node.moduleName}` : node.token;
      const addNodeAndEdge =
        !searchValue || node.childEdgeMatch || node.parentEdgeMatch || node.match;

      if (!addNodeAndEdge) {
        return;
      }

      if (parentNodeId) {
        if (!edges.find((e) => e.from === parentNodeId && e.to === nodeId)) {
          edges.push({ from: parentNodeId, to: nodeId });
        }
      }

      if (!nodes.find((n) => n.id === nodeId)) {
        nodes.push({
          id: nodeId,
          label: node.moduleName ? `<b>${node.moduleName}</b> | ${node.token}` : node.token,
          multiInstance: node.multiInstance,
          match: node.match,
        });
      }

      if (!groupsRank[nodeId] || groupsRank[nodeId] < node.rank) {
        groupsRank[nodeId] = node.rank;
      }

      node.deps.forEach((d) => {
        recurse(node.multi ? parentNodeId : nodeId, d);
      });
    }

    recurse(null, rootGraph);

    Object.keys(groupsRank).forEach((nodeId) => {
      const rank = groupsRank[nodeId];
      groupsByRank[rank] = groupsByRank[rank] || [];
      groupsByRank[rank].push(nodeId);
    });

    const res = !nodes.length
      ? ''
      : `digraph g {
rankdir = "LR";
nodesep=0.1;
ranksep=0.3;
compound=true;
concentrate=true;
center=true;
node [fontsize = "16", shape = "record", height=0.1, color=lightblue2];
edge [];
${nodes
  .map((n) => {
    return `"${n.id}"[label=<${n.label}>]${n.multiInstance ? '[color="gold"]' : ''}${
      n.match ? '[color="red"]' : ''
    };`;
  })
  .join('\n')}
${edges
  .map((e) => {
    return `"${e.from}"->"${e.to}";`;
  })
  .join('\n')}
${Object.values(groupsByRank)
  .map((ids) => {
    return `{ rank = "same"; ${ids.map((id) => `"${id}"`).join(';')} }`;
  })
  .join('\n')}
}`;

    return res;
  }

  return Object.values(commandLineListTokens)
    .map((k): [string, RecordsGraph] | null => {
      const g = buildDepsGraph(di, null, 0, k.toString());
      return g ? [k.toString(), g] : null;
    })
    .filter(Boolean)
    .map(([k, g]) => [k, formatGraphToDot(g)]);
};
