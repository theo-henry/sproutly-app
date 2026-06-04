import { Canvas, useFrame } from "@react-three/fiber";
import { EffectComposer, Bloom, Vignette } from "@react-three/postprocessing";
import { useMemo, useRef } from "react";
import * as THREE from "three";

const LOOP = 9; // seconds

const ease = (t: number) => (t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2);
const clamp01 = (t: number) => Math.max(0, Math.min(1, t));

// Per-frame loop phase in [0, 1).
function usePhase() {
  const ref = useRef(0);
  useFrame(({ clock }) => {
    ref.current = (clock.elapsedTime % LOOP) / LOOP;
  });
  return ref;
}

// Global growth/fade envelope — masks the loop seam.
function growthAt(phase: number) {
  // grow 0 → 0.55, hold 0.55 → 0.88, fade 0.88 → 1.0
  if (phase < 0.55) return ease(phase / 0.55);
  if (phase < 0.88) return 1;
  return 1 - ease((phase - 0.88) / 0.12);
}

function Soil() {
  // Low dome with a subtle crack made from two thin emissive slivers.
  const moundGeo = useMemo(() => {
    const g = new THREE.SphereGeometry(1.4, 64, 64, 0, Math.PI * 2, 0, Math.PI / 2);
    g.scale(1, 0.18, 1);
    // Roughen the surface a touch.
    const pos = g.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i);
      const y = pos.getY(i);
      const z = pos.getZ(i);
      const n = Math.sin(x * 9 + z * 7) * 0.012 + Math.cos(x * 14 - z * 11) * 0.008;
      pos.setY(i, y + n);
    }
    g.computeVertexNormals();
    return g;
  }, []);

  const phaseRef = usePhase();
  const crackRef = useRef<THREE.Group>(null);

  useFrame(() => {
    if (!crackRef.current) return;
    const p = phaseRef.current;
    // Crack visible briefly before the sprout appears.
    const v = clamp01(p / 0.08) * (1 - clamp01((p - 0.08) / 0.12));
    crackRef.current.scale.x = 0.2 + v * 0.6;
    (crackRef.current.children[0] as THREE.Mesh).material &&
      ((crackRef.current.children[0] as any).material.opacity = v * 0.9);
  });

  return (
    <group>
      <mesh geometry={moundGeo} receiveShadow position={[0, 0, 0]}>
        <meshStandardMaterial
          color="#1b1410"
          roughness={1}
          metalness={0}
        />
      </mesh>
      {/* Soil rim ring — slight contrast so the mound reads against the floor. */}
      <mesh position={[0, 0.005, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <ringGeometry args={[1.25, 1.4, 96]} />
        <meshBasicMaterial color="#0a0806" transparent opacity={0.6} />
      </mesh>
      {/* Crack — thin emissive line on the soil. */}
      <group ref={crackRef} position={[0, 0.255, 0]} rotation={[-Math.PI / 2, 0, 0.4]}>
        <mesh>
          <planeGeometry args={[0.32, 0.012]} />
          <meshBasicMaterial color="#7dffa8" transparent opacity={0} />
        </mesh>
      </group>
      {/* Subtle floor catches rim light. */}
      <mesh rotation={[-Math.PI / 2, 0, 0]} position={[0, -0.001, 0]} receiveShadow>
        <circleGeometry args={[6, 96]} />
        <meshStandardMaterial color="#070707" roughness={0.85} metalness={0.05} />
      </mesh>
    </group>
  );
}

// Curved leaf — a Shape extruded into a ShapeGeometry, then bent on Z.
function useLeafGeometry() {
  return useMemo(() => {
    const shape = new THREE.Shape();
    shape.moveTo(0, 0);
    shape.bezierCurveTo(0.28, 0.18, 0.46, 0.78, 0, 1.4);
    shape.bezierCurveTo(-0.46, 0.78, -0.28, 0.18, 0, 0);
    const geo = new THREE.ShapeGeometry(shape, 40);
    const pos = geo.attributes.position;
    for (let i = 0; i < pos.count; i++) {
      const x = pos.getX(i);
      const y = pos.getY(i);
      // Bend backwards in z to give the leaf body.
      const z = -0.22 * x * x - 0.04 * y + 0.02 * Math.sin(y * 6);
      pos.setZ(i, z);
    }
    geo.computeVertexNormals();
    return geo;
  }, []);
}

type LeafProps = {
  geometry: THREE.BufferGeometry;
  delay: number; // 0..1 within growth window
  angle: number; // azimuth around stem
  heightOn: number; // 0..1 stem position
  size: number;
  tilt: number; // base outward tilt
  fadeRef: React.MutableRefObject<number>;
  phaseRef: React.MutableRefObject<number>;
};

function Leaf({ geometry, delay, angle, heightOn, size, tilt, fadeRef, phaseRef }: LeafProps) {
  const pivot = useRef<THREE.Group>(null);
  const tiltRef = useRef<THREE.Group>(null);
  const matRef = useRef<THREE.MeshStandardMaterial>(null);

  useFrame(() => {
    const phase = phaseRef.current;
    const g = growthAt(phase);
    // Each leaf has its own emerge window within the grow phase.
    const growWindow = 0.55;
    const local = clamp01((phase - delay * growWindow) / 0.22);
    const e = ease(local);

    if (pivot.current) {
      const s = (0.0001 + e * size) * (0.85 + g * 0.15);
      pivot.current.scale.setScalar(s);
      pivot.current.position.y = 0.18 + heightOn * 0.55 * g;
      pivot.current.rotation.y = angle + phase * Math.PI * 2 * 0.18; // slow co-rotation
    }
    if (tiltRef.current) {
      // Leaf unfurls from pointing up to outward tilt.
      tiltRef.current.rotation.x = -tilt * e - (1 - e) * 1.55;
      tiltRef.current.rotation.z = Math.sin(phase * Math.PI * 2 + angle) * 0.04 * e;
    }
    if (matRef.current) {
      matRef.current.opacity = fadeRef.current;
      matRef.current.emissiveIntensity = 0.18 + 0.12 * Math.sin(phase * Math.PI * 2 + angle);
    }
  });

  return (
    <group ref={pivot}>
      <group ref={tiltRef}>
        <mesh geometry={geometry} castShadow>
          <meshStandardMaterial
            ref={matRef}
            color="#3fa055"
            emissive="#0e4a1c"
            emissiveIntensity={0.25}
            roughness={0.45}
            metalness={0.05}
            side={THREE.DoubleSide}
            transparent
          />
        </mesh>
      </group>
    </group>
  );
}

function Stem({ phaseRef, fadeRef }: { phaseRef: React.MutableRefObject<number>; fadeRef: React.MutableRefObject<number> }) {
  const tubeGeo = useMemo(() => {
    const curve = new THREE.CatmullRomCurve3([
      new THREE.Vector3(0, 0, 0),
      new THREE.Vector3(0.03, 0.25, 0.02),
      new THREE.Vector3(-0.02, 0.5, -0.02),
      new THREE.Vector3(0.015, 0.78, 0.01),
      new THREE.Vector3(0, 1.0, 0),
    ]);
    return new THREE.TubeGeometry(curve, 48, 0.018, 12, false);
  }, []);

  const ref = useRef<THREE.Mesh>(null);
  const matRef = useRef<THREE.MeshStandardMaterial>(null);

  useFrame(() => {
    const phase = phaseRef.current;
    const g = growthAt(phase);
    if (ref.current) {
      ref.current.scale.set(1, g, 1);
      ref.current.position.y = 0.18;
    }
    if (matRef.current) matRef.current.opacity = fadeRef.current;
  });

  return (
    <mesh ref={ref} geometry={tubeGeo} castShadow>
      <meshStandardMaterial
        ref={matRef}
        color="#4aa15a"
        emissive="#0a3c18"
        emissiveIntensity={0.2}
        roughness={0.5}
        transparent
      />
    </mesh>
  );
}

// Faint glowing rings beneath the soil — sustainable-tech hint.
function EnergyLines() {
  const group = useRef<THREE.Group>(null);
  const phaseRef = usePhase();

  useFrame(() => {
    if (!group.current) return;
    const p = phaseRef.current;
    group.current.children.forEach((c, i) => {
      const m = (c as THREE.Mesh).material as THREE.MeshBasicMaterial;
      const pulse = 0.25 + 0.3 * Math.sin(p * Math.PI * 2 + i * 0.7);
      m.opacity = pulse;
      c.rotation.z = p * Math.PI * 2 * (i % 2 === 0 ? 0.15 : -0.12) + i;
    });
  });

  const rings = [0.7, 1.05, 1.45, 1.95];
  return (
    <group ref={group} position={[0, -0.02, 0]} rotation={[-Math.PI / 2, 0, 0]}>
      {rings.map((r, i) => (
        <mesh key={i}>
          <ringGeometry args={[r, r + 0.006, 128]} />
          <meshBasicMaterial color="#37d181" transparent opacity={0.3} side={THREE.DoubleSide} />
        </mesh>
      ))}
    </group>
  );
}

// Holographic vertical rings around the plant.
function HoloRings() {
  const g1 = useRef<THREE.Mesh>(null);
  const g2 = useRef<THREE.Mesh>(null);
  const phaseRef = usePhase();

  useFrame(() => {
    const p = phaseRef.current;
    if (g1.current) {
      g1.current.rotation.y = p * Math.PI * 2 * 0.4;
      g1.current.rotation.x = Math.PI / 2 + Math.sin(p * Math.PI * 2) * 0.08;
    }
    if (g2.current) {
      g2.current.rotation.y = -p * Math.PI * 2 * 0.3;
      g2.current.rotation.x = Math.PI / 2.2;
    }
  });

  return (
    <group position={[0, 0.55, 0]}>
      <mesh ref={g1}>
        <torusGeometry args={[1.1, 0.004, 16, 200]} />
        <meshBasicMaterial color="#9bffc6" transparent opacity={0.18} />
      </mesh>
      <mesh ref={g2}>
        <torusGeometry args={[1.35, 0.003, 16, 200]} />
        <meshBasicMaterial color="#6fe0a8" transparent opacity={0.12} />
      </mesh>
    </group>
  );
}

// Drifting motes — kept sparse so the scene stays calm.
function Motes() {
  const count = 90;
  const ref = useRef<THREE.Points>(null);

  const { positions, seeds } = useMemo(() => {
    const positions = new Float32Array(count * 3);
    const seeds = new Float32Array(count);
    for (let i = 0; i < count; i++) {
      const r = 1.2 + Math.random() * 1.8;
      const a = Math.random() * Math.PI * 2;
      positions[i * 3 + 0] = Math.cos(a) * r;
      positions[i * 3 + 1] = Math.random() * 2.2;
      positions[i * 3 + 2] = Math.sin(a) * r;
      seeds[i] = Math.random();
    }
    return { positions, seeds };
  }, []);

  useFrame(({ clock }) => {
    if (!ref.current) return;
    const t = clock.elapsedTime;
    const pos = ref.current.geometry.attributes.position as THREE.BufferAttribute;
    for (let i = 0; i < count; i++) {
      const s = seeds[i];
      const baseY = (i * 0.137) % 1;
      const y = ((baseY + t * 0.04 * (0.5 + s)) % 1) * 2.6 - 0.1;
      pos.setY(i, y);
      const x = pos.getX(i);
      const z = pos.getZ(i);
      const wobble = Math.sin(t * 0.6 + s * 10) * 0.0008;
      pos.setX(i, x + wobble);
      pos.setZ(i, z - wobble);
    }
    pos.needsUpdate = true;
  });

  return (
    <points ref={ref}>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" args={[positions, 3]} />
      </bufferGeometry>
      <pointsMaterial
        size={0.012}
        color="#bff5d4"
        transparent
        opacity={0.55}
        depthWrite={false}
        sizeAttenuation
      />
    </points>
  );
}

function Camera() {
  const phaseRef = usePhase();
  useFrame(({ camera }) => {
    const p = phaseRef.current;
    // Slow push-in then gentle hold; orbit a few degrees.
    const pushIn = ease(clamp01(p / 0.7));
    const radius = 4.2 - pushIn * 0.7;
    const orbit = p * Math.PI * 2 * 0.08; // very slow
    camera.position.x = Math.sin(orbit) * radius * 0.25;
    camera.position.z = Math.cos(orbit) * radius;
    camera.position.y = 1.05 + Math.sin(p * Math.PI * 2) * 0.04;
    camera.lookAt(0, 0.55, 0);
  });
  return null;
}

function Plant() {
  const leafGeo = useLeafGeometry();
  const phaseRef = usePhase();
  const fadeRef = useRef(1);
  const rotRef = useRef<THREE.Group>(null);

  useFrame(() => {
    const p = phaseRef.current;
    fadeRef.current = growthAt(p);
    if (rotRef.current) {
      // The plant itself rotates slowly, synced with the camera orbit but opposite.
      rotRef.current.rotation.y = p * Math.PI * 2 * 0.5;
    }
  });

  // Six leaves staggered around the stem.
  const leaves = [
    { delay: 0.15, angle: 0.0, heightOn: 0.35, size: 0.42, tilt: 0.7 },
    { delay: 0.22, angle: Math.PI * 0.66, heightOn: 0.42, size: 0.4, tilt: 0.75 },
    { delay: 0.3, angle: Math.PI * 1.33, heightOn: 0.5, size: 0.45, tilt: 0.65 },
    { delay: 0.42, angle: Math.PI * 0.33, heightOn: 0.7, size: 0.35, tilt: 0.5 },
    { delay: 0.52, angle: Math.PI * 1.0, heightOn: 0.78, size: 0.32, tilt: 0.45 },
    { delay: 0.62, angle: Math.PI * 1.66, heightOn: 0.86, size: 0.3, tilt: 0.35 },
  ];

  return (
    <group ref={rotRef}>
      <Stem phaseRef={phaseRef} fadeRef={fadeRef} />
      {leaves.map((l, i) => (
        <Leaf key={i} geometry={leafGeo} {...l} fadeRef={fadeRef} phaseRef={phaseRef} />
      ))}
    </group>
  );
}

function Scene() {
  return (
    <>
      <color attach="background" args={["#050607"]} />
      <fog attach="fog" args={["#050607", 4, 12]} />

      {/* Lighting — soft rim from behind, warm key, green underlight. */}
      <ambientLight intensity={0.18} />
      <directionalLight position={[3, 5, 2]} intensity={1.3} color="#fff7e6" castShadow />
      <directionalLight position={[-3.5, 3, -3]} intensity={1.6} color="#9bffc6" />
      <pointLight position={[0, -0.4, 0]} intensity={1.2} color="#37d181" distance={2.6} decay={2} />
      <pointLight position={[0, 1.6, 0.4]} intensity={0.5} color="#cffce0" distance={3} decay={2} />

      <Camera />
      <Soil />
      <EnergyLines />
      <Plant />
      <HoloRings />
      <Motes />

      <EffectComposer>
        <Bloom intensity={0.65} luminanceThreshold={0.22} luminanceSmoothing={0.5} mipmapBlur />
        <Vignette eskil={false} offset={0.25} darkness={0.85} />
      </EffectComposer>
    </>
  );
}

type Props = { className?: string };

export default function PlantHero({ className }: Props) {
  return (
    <div className={className} aria-hidden="true">
      <Canvas
        dpr={[1, 1.5]}
        gl={{ antialias: true, powerPreference: "high-performance" }}
        camera={{ fov: 32, position: [0, 1.05, 4.2], near: 0.1, far: 50 }}
        shadows={false}
      >
        <Scene />
      </Canvas>
    </div>
  );
}
