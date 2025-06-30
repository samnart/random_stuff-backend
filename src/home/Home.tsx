import React, { useState } from "react";
import {
    Menu,
    X,
    MapPin,
    Users,
    Briefcase,
    Calendar,
    TrendingUp,
    Building2,
    Globe,
    ArrowRight,
    Star,
    Play,
    Phone,
    Mail,
    Facebook,
    Twitter,
    Instagram,
    ChevronDown,
} from "lucide-react";

const Home = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [activeUserType, setActiveUsertype] = useState('local');

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
            {/* Header */}
            <header className="bg-white/90 backdrop-blur-md shadow-lg sticky top-0 z-50">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center py-4">
                        <div className="flex items-center space-x-3">
                            <div className="w-10 h-10 bg-gradient-to-r from-blue-600 to-green-600 rounded-lg flex items-center justify-center">
                                <Building2 className="h-6 w-6 text-white" />
                            </div>
                            <div> 
                                <h1 className="text-xl font-bold bg-gradient-to-r from-blue-600 to-green-600 bg-clip-text text-transparent">
                                    EnchiConnect
                                </h1>
                                <p className="text-xs text-gray-600">Building Tomorrow Together</p>
                            </div>
                        </div>

                        {/* {Desktop Navigation} */}
                        <nav className="hidden md:flex items-center space-x-8">
                            <a href="#home" className="text-gray-700 hover:text-blue-600 transition-colors">Home</a>
                            <a href="#community" className="text-gray-700 hover:text-blue-600 transition-colors">Community</a>
                            <a href="#jobs" className="text-gray-700 hover:text-blue-600 transition-colors">Jobs</a>
                            <a href="#events" className="text-gray-700 hover:text-blue-600 transition-colors">Events</a>
                            <a href="#invest" className="text-gray-700 hover:text-blue-600 transition-colors">Invest</a>
                            <a href="#visit" className="text-gray-700 hover:text-blue-600 transition-colors">Visit</a>
                        </nav>

                        <div className="flex items-center space-x-4">
                            <button className="hidden md:block bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors">
                                Sign In
                            </button>
                            <button className="hidden md:block bg-gradient-to-r from-green-600 to-blue-600 text-white px-4 py-2 rounded-lg hover:opacity-90 transition-opacity">
                                Join Community
                            </button>

                            {/* Mobile menu button */}
                            <button
                                onClick={() => setIsMenuOpen(!isMenuOpen)}
                                className="md:hidden p-2"
                            >
                                {isMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h6 w-6" />}
                            </button>
                        </div>
                    </div>
                </div>
            </header>
        </div>
    );
};

export default Home;
