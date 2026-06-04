import LandingSlider from "../components/LandingSlider";

// Landing route just mounts the slider. Kept thin so the page is easy to
// swap (A/B tests, marketing variants, etc.).
export default function Landing() {
  return <LandingSlider />;
}
