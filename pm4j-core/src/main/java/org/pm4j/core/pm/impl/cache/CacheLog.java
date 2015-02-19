package org.pm4j.core.pm.impl.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pm4j.core.pm.PmObject;
import org.pm4j.core.pm.impl.PmUtil;

// TODO oboede: remove the PM dependency.
public final class CacheLog {

  public static final CacheLog INSTANCE = new CacheLog();

  private static final Logger LOG = LoggerFactory.getLogger(CacheLog.class);

  private Map<String, Long> pmCacheHitMap = new ConcurrentHashMap<String, Long>();
  private Map<String, Long> pmCacheInitMap = new ConcurrentHashMap<String, Long>();

  public final void logPmCacheHit(PmObject pm, String cacheItem) {
    if (LOG.isTraceEnabled()) {
      doLog(pm, cacheItem, pmCacheHitMap, "hit");
    }
  }

  public final void logPmCacheInit(PmObject pm, String cacheItem) {
    if (LOG.isTraceEnabled()) {
      doLog(pm, cacheItem, pmCacheInitMap, "init");
    }
  }

  private final void doLog(PmObject pm, String cacheItem, Map<String, Long> counterMap, String mapKind) {
    String key = PmUtil.getAbsoluteName(pm) + "-" + cacheItem;
    Long count = counterMap.get(key);
    count = (count == null) ? 1L : ++count;

    counterMap.put(key, count);

    double countSqrt = Math.sqrt(count);
    if ((Math.ceil(countSqrt) - countSqrt) == 0) {
      LOG.trace("Pm cache " + mapKind + "  for: " + key + getHitRatio(key));
    }
  }

  private String getHitRatio(String key) {
    Long inits = pmCacheInitMap.get(key);
    Long hits = pmCacheHitMap.get(key);
    inits = (inits == null) ? 0 : inits;
    hits = (hits == null) ? 0 : hits;

    return " hits/inits=" + hits + "/" + inits;
  }

}
