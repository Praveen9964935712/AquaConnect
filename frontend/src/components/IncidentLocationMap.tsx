import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

export type IncidentMapPoint = {
  id: string;
  label: string;
  latitude: number;
  longitude: number;
  source?: string | null;
};

export default function IncidentLocationMap({ points }: { points: IncidentMapPoint[] }) {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const container = containerRef.current;
    if (!container || points.length === 0) return;

    const map = L.map(container, { scrollWheelZoom: false, zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap contributors',
      maxZoom: 19
    }).addTo(map);

    const bounds = L.latLngBounds([]);
    points.forEach((point) => {
      const marker = L.marker([point.latitude, point.longitude]).addTo(map);
      marker.bindPopup(`<strong>${escapeHtml(point.label)}</strong><br>${escapeHtml(point.source ?? 'Location source unavailable')}`);
      bounds.extend([point.latitude, point.longitude]);
    });

    map.fitBounds(bounds, { padding: [24, 24], maxZoom: 13 });
    return () => { map.remove(); };
  }, [points]);

  if (points.length === 0) {
    return <div className="map-placeholder"><strong>No mapped incidents</strong><span>Location coordinates were not returned for the current records.</span></div>;
  }

  return <div className="map-frame" ref={containerRef} role="region" aria-label="Map of incidents with supplied coordinates" />;
}

function escapeHtml(value: string) {
  return value.replace(/[&<>"']/g, (character) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[character] ?? character);
}
